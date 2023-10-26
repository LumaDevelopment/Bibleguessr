# Bible Formatter

The `bibles` database in the `bible-service` directory was made using a spreadsheet containing 14 different English versions of the Bible.

`bibles.csv` is the spreadsheet while `bible_database_maker.py` is a simple python script that reads through the csv file, creates individual txt files out of the columns of the csv file, and populates the txt files with verses (1 per line).

The txt files are formatted where there is an agreed upon marker that separates book names followed by the book name itself. An example would be "|Genesis" on one line, followed by all the verses in that book, each one on their own line. 

Every bible in the database is public domain or free for programs to use.

We are currently searching for more public domain bible databases (especially ones in different languages). Once we find one that is adequate, more python scripts will be created to put the database into a format we can work with.

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
