from csv import reader
import re

with open("bibles.csv", "r", encoding='cp1252') as file:
    lines = []
    csv_file = reader(file)
    for row in csv_file:
        lines.append(row)
    for colIndex in range(1, 15):
        with open(lines[1][colIndex]+".txt", "w") as database:
            print("Currently working on: "+lines[1][colIndex])
            difference = ""
            for rowIndex in range(2, len(lines)):
                pattern = r"^\d?[\s\w]+?(?=\s\d)"
                book_name = re.search(pattern, lines[rowIndex][0]).group()
                if book_name != difference:
                    difference = book_name
                    database.write("|"+book_name+"\n"+lines[rowIndex][colIndex]+"\n")
                else:
                    database.write(lines[rowIndex][colIndex]+"\n")
print("Done!")