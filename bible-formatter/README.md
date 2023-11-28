# Bible Formatter

The `bibles` database in the `bible-service` directory was made using a spreadsheet containing 14 different English versions of the Bible.

`bibles.csv` is the spreadsheet while `bible_database_maker.py` is a simple Python script that reads through the CSV file, creates individual txt files out of the columns of the CSV file, and populates the txt files with verses (1 per line).

The text files are formatted with an agreed-upon marker that separates book names followed by the book name itself. An example would be "|Genesis" on one line, followed by all the verses in that book, each on their own line. 

`bible_batch_1` is a directory containing the first batch of raw bible databases in different languages. The script `bible_version_adder.py` was created to go through each file in the directory, and if it's a CSV, open it and format the data into a text file.

Every bible in the database is public domain or free for programs to use.

We are currently searching for more public-domain bible databases (especially ones in different languages). Once we find one that is adequate, more Python scripts will be created to put the database into a format we can work with.

Raw bible data provided by Bible Supersearch (https://www.biblesupersearch.com/) and Bible Hub (https://biblehub.net/)

## Here are the current languages we have thus far:
  - **Staten Vertaling** - Dutch
  - **Synodal** - Russian
  - **Bible Kralicka** - Czech
  - **Biblia Livre** - Portuguese
  - **Cornilescu** - Romanian
  - **Diodati** - Italian
  - **Finnish 1776** - Finnish
  - **Indian Revised Version** - Hindi
  - **Karoli** - Hungarian
  - **Korean** - Korean
  - **Louis Segond 1910** - French
  - **Luther Bible** - German
  - **Polska Biblia Gdanska** - Polish
  - **Reina Valera 1909** - Spanish
