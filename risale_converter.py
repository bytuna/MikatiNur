import pdfplumber
import json
import os
import glob

# YAPILANDIRMA
INPUT_FOLDER = "kitaplar"  # PDF'lerin olduğu klasör
OUTPUT_BASE_PATH = "app/src/main/assets/risale"

def is_red(color):
    """PDF içindeki kırmızı tonlarını algılar (RGB)"""
    if not color: return False
    if len(color) == 3:
        r, g, b = color
        # Risale PDF'lerinde genelde kırmızı kanal yüksek, diğerleri düşüktür
        return r > 0.5 and g < 0.3 and b < 0.3
    return False

def process_pdf(pdf_path):
    book_id = os.path.basename(pdf_path).replace(".pdf", "").lower()
    output_json = os.path.join(OUTPUT_BASE_PATH, f"{book_id}.json")
    pages_data = []

    print(f"\n--- {book_id.upper()} işleniyor ---")

    try:
        with pdfplumber.open(pdf_path) as pdf:
            total_pages = len(pdf.pages)
            for i, page in enumerate(pdf.pages):
                page_content = ""
                words = page.extract_words(extra_attrs=["non_stroking_color"])

                last_color_was_red = False
                for word in words:
                    text = word['text']
                    color = word.get('non_stroking_color')

                    if is_red(color):
                        if not last_color_was_red:
                            page_content += " <font color='#D32F2F'>"
                        page_content += text + " "
                        last_color_was_red = True
                    else:
                        if last_color_was_red:
                            page_content += "</font> "
                        page_content += text + " "
                        last_color_was_red = False

                if last_color_was_red:
                    page_content += "</font>"

                pages_data.append({
                    "bookId": book_id,
                    "pageNumber": i + 1,
                    "content": page_content.strip()
                })

                if (i + 1) % 20 == 0 or i + 1 == total_pages:
                    print(f"Ilerleme: {i+1}/{total_pages} sayfa tamamlandı.")

        # JSON Olarak Kaydet
        os.makedirs(os.path.dirname(output_json), exist_ok=True)
        with open(output_json, 'w', encoding='utf-8') as f:
            json.dump(pages_data, f, ensure_ascii=False, indent=2)
        print(f"BİTTİ: {output_json} oluşturuldu.")

    except Exception as e:
        print(f"HATA ({book_id}): {str(e)}")

def main():
    if not os.path.exists(INPUT_FOLDER):
        os.makedirs(INPUT_FOLDER)
        print(f"'{INPUT_FOLDER}' klasörü oluşturuldu. Lütfen PDF'lerinizi içine atın.")
        return

    pdf_files = glob.glob(os.path.join(INPUT_FOLDER, "*.pdf"))

    if not pdf_files:
        print(f"'{INPUT_FOLDER}' klasöründe PDF bulunamadı!")
        return

    print(f"Toplam {len(pdf_files)} kitap bulundu. İşlem başlıyor...")

    for pdf_file in pdf_files:
        process_pdf(pdf_file)

    print("\n==========================================")
    print("TÜM KÜLLİYAT BAŞARIYLA DÖNÜŞTÜRÜLDÜ!")
    print("==========================================")

if __name__ == "__main__":
    main()
