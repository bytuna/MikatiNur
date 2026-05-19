import requests
from bs4 import BeautifulSoup
import json
import time
import os

def scrape_one_book(book_id, slug, total_pages):
    base_url = "http://www.erisale.com/"
    all_pages = []
    print(f"\n>>> {slug.upper()} Çekiliyor ({total_pages} sayfa)...")

    for page_num in range(1, total_pages + 1):
        params = {'locale': 'tr', 'bookId': book_id, 'pageNo': page_num}
        try:
            response = requests.get(base_url, params=params, timeout=15)
            response.encoding = 'utf-8'
            if response.status_code == 200:
                soup = BeautifulSoup(response.text, 'html.parser')
                noscript = soup.find('noscript')
                content = ""
                if noscript:
                    paragraphs = noscript.find_all('p')
                    for p in paragraphs:
                        text = p.get_text().strip()
                        if not text or "Sayfa bulunamadi" in text: continue
                        p_class = " ".join(p.get('class', [])) if p.get('class') else ""
                        if 'arapca' in p_class.lower():
                            content += f"<font color='#D32F2F'>{text}</font>\n"
                        else:
                            content += f"{text}\n"

                if not content.strip(): content = "(Boş Sayfa)"
                all_pages.append({"bookId": slug, "pageNumber": page_num, "content": content.strip()})
                if page_num % 100 == 0: print(f"   Progress: {page_num}/{total_pages}")
            time.sleep(0.01)
        except Exception as e:
            print(f"Hata S:{page_num} -> {e}")

    output_path = f"app/src/main/assets/risale/{slug}.json"
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(all_pages, f, ensure_ascii=False, indent=2)
    print(f"DONE: {slug}.json")

if __name__ == "__main__":
    import sys
    if len(sys.argv) > 3:
        scrape_one_book(int(sys.argv[1]), sys.argv[2], int(sys.argv[3]))
