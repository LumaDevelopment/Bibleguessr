# It's not pretty, but io_util.py
# should be shared between stage one
# and stage two, so we're adding it
# to the path here
import sys

sys.path.append('..')

from io_util import get_input_output_files


# Abbreviations from OSIS
abbreviations = {
    "Genesis": "GEN",
    "Exodus": "EXO",
    "Leviticus": "LEV",
    "Numbers": "NUM",
    "Deuteronomy": "DEU",
    "Joshua": "JOS",
    "Judges": "JDG",
    "Ruth": "RUT",
    "1 Samuel": "1SA",
    "2 Samuel": "2SA",
    "1 Kings": "1KI",
    "2 Kings": "2KI",
    "1 Chronicles": "1CH",
    "2 Chronicles": "2CH",
    "Ezra": "EZR",
    "Nehemiah": "NEH",
    "Esther": "EST",
    "Job": "JOB",
    "Psalm": "PSA",
    "Proverbs": "PRO",
    "Ecclesiastes": "ECC",
    "Song of Solomon": "SNG",
    "Isaiah": "ISA",
    "Jeremiah": "JER",
    "Lamentations": "LAM",
    "Ezekiel": "EZK",
    "Daniel": "DAN",
    "Hosea": "HOS",
    "Joel": "JOL",
    "Amos": "AMO",
    "Obadiah": "OBA",
    "Jonah": "JON",
    "Micah": "MIC",
    "Nahum": "NAM",
    "Habakkuk": "HAB",
    "Zephaniah": "ZEP",
    "Haggai": "HAG",
    "Zechariah": "ZEC",
    "Malachi": "MAL",
    "Matthew": "MAT",
    "Mark": "MRK",
    "Luke": "LUK",
    "John": "JHN",
    "Acts": "ACT",
    "Romans": "ROM",
    "1 Corinthians": "1CO",
    "2 Corinthians": "2CO",
    "Galatians": "GAL",
    "Ephesians": "EPH",
    "Philippians": "PHP",
    "Colossians": "COL",
    "1 Thessalonians": "1TH",
    "2 Thessalonians": "2TH",
    "1 Timothy": "1TI",
    "2 Timothy": "2TI",
    "Titus": "TIT",
    "Philemon": "PHM",
    "Hebrews": "HEB",
    "James": "JAS",
    "1 Peter": "1PE",
    "2 Peter": "2PE",
    "1 John": "1JN",
    "2 John": "2JN",
    "3 John": "3JN",
    "Jude": "JUD",
    "Revelation": "REV"
}


# This method:
# 1) Reformat the verse reference according to
#    an OSIS-like verse reference standard.
# 2) Grabs the text of the verse from the original line
# 3) Assembles new line with reference and text concatenated
#    by a period
# 4) Returns the formatted line
def reformat_line(line):
    # Key challenges:
    # 1) Some book names have spaces in them
    # 2) Verse references are separated from text by tabs.

    # First, find the colon.
    colon_index = line.find(":")

    # Get the chapter index by going 4 spaces
    # back from the colon and finding the first
    # space. This exploits the knowledge that
    # 1) the largest chapter number in the Bible is 3 digits
    # 2) Even if the chapter is Job 1 the starting index
    #    never goes below 0
    chapter_index = line.find(" ", colon_index - 4) + 1
    chapter_num = int(line[chapter_index:colon_index])

    # Select from 0 to chapter index - 1 to get
    # the book name
    book = line[0:chapter_index - 1]

    # Get tab index (for verse number and for text)
    tab_index = line.find("\t")

    # Get the verse number by going from colon
    # index to first tab
    verse_num = int(line[colon_index + 1:tab_index])

    # Get the text by going from the tab index
    # to the end of the line
    text = line[tab_index + 1:]

    # Create an OSIS-like reference
    reference = f"{abbreviations[book]}.{chapter_num:03d}.{verse_num:03d}"

    # Return the reformatted line
    return f"{reference}.{text}"


def main():
    # These files should already be processed
    # from stage one, so we'll assume they are
    # utf-8 encoded
    input_file_encoding = "utf-8"
    output_file_name_suffix = ""

    # Pass off boilerplate file processing
    # code to io_util.py
    files = get_input_output_files(input_file_encoding, output_file_name_suffix)

    # Grab files from the tuple
    input_file = files[0]
    output_file = files[1]

    # In a similar fashion to stage one,
    # read the file line by line
    line = input_file.readline()

    # Keep reading lines and re-formatting them while also
    # cutting out the header line and any blank lines
    while line:
        if not (line.startswith("Verse") or len(line.strip()) == 0):
            output_file.write(reformat_line(line))
        line = input_file.readline()

    # Close the files
    input_file.close()
    output_file.close()


if __name__ == "__main__":
    main()
