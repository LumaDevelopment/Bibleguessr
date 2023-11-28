from pathlib import Path
from csv import reader

path_name = "bible_batch_1"
folder_path = Path(path_name)

if folder_path.is_dir():
    for file_path in folder_path.iterdir():
        if file_path.is_file() and file_path.suffix.lower() == ".csv":
            with open(str(file_path), "r", encoding="UTF-8") as bible:
                lines = []
                difference = ""
                csv_file = reader(bible)
                for row in csv_file:
                    lines.append(row)
                with open(lines[0][0]+".txt", "w", encoding="UTF-8") as output:
                    print(f"Currently working on: {lines[0][0]}")
                    for i in range(6, len(lines)):
                        book_name = lines[i][1]
                        if book_name != difference:
                            output.write(f"|{book_name}\n")
                            difference = book_name
                        output.write(f"{lines[i][5]}\n")
        else:
            print(f"Error - {file_path.name} is not a CSV file")
else:
    print("Error - Cannot find specified bible directory")
    
print("Done!")