import requests
from bs4 import BeautifulSoup
import json
import time
import sys

def scrape_range(book_id, slug, start, end):
    base_url = "http://www.erisale.com/"
    pages = []
    print(f"{slug.upper()} {start}-{end} çekiliyor...")
    for page_num in range(start, end + 1):
        params = {'locale': 'tr', 'bookId': book_id, 'pageNo': page_num}
        try:
            r = requests.get(base_url, params=params, timeout=10)
            r.encoding = 'utf-8'
            soup = BeautifulSoup(r.text, 'html.parser')
            noscript = soup.find('noscript')
            content = ""
            if noscript:
                for p in noscript.find_all('p'):
                    text = p.get_text().strip()
                    if not text or "Sayfa bulunamadi" in text: continue
                    p_class = " ".join(p.get('class', [])) if p.get('class') else ""
                    if 'arapca' in p_class.lower(): content += f"<font color='#D32F2F'>{text}</font>\n"
                    else: content += f"{text}\n"
            if not content.strip(): content = "(Boş Sayfa)"
            pages.append({"bookId": slug, "pageNumber": page_num, "content": content.strip()})
        except: pass
        if page_num % 50 == 0: print(f"   Progress: {page_num}")

    with open(f"{slug}_{start}_{end}.json", 'w', encoding='utf-8') as f:
        json.dump(pages, f, ensure_ascii=False)

if __name__ == "__main__":
    scrape_range(int(sys.argv[1]), sys.argv[2], int(sys.argv[3]), int(sys.argv[4]))
