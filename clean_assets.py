import json
import os

path = 'app/src/main/assets/risale/'
placeholder = '(Bu sayfa orijinal kitap numarasında metin içermemektedir.)'

results = {}

for file in os.listdir(path):
    if file.endswith('.json') and file != 'lugat.json':
        full_path = os.path.join(path, file)
        try:
            with open(full_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            # Gerçek içeriği olan sayfaları filtrele
            cleaned = [p for p in data if p['content'].strip() and placeholder not in p['content']]

            if cleaned:
                first_page = cleaned[0]['pageNumber']
                last_page = cleaned[-1]['pageNumber']
                slug = file.replace('.json', '')
                results[slug] = (first_page, last_page, len(cleaned))

                with open(full_path, 'w', encoding='utf-8') as f:
                    json.dump(cleaned, f, ensure_ascii=False, indent=2)
                print(f"Temizlendi: {file} (Başlangıç: {first_page}, Bitiş: {last_page})")
        except Exception as e:
            print(f"Hata {file}: {e}")

print("\n--- KOD GÜNCELLEMESİ İÇİN VERİLER ---")
for slug, info in results.items():
    print(f"Book: {slug} -> Start: {info[0]}, End: {info[1]}, Count: {info[2]}")
