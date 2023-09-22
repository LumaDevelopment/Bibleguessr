import html
import re

# It's not pretty, but io_util.py
# should be shared between stage one
# and stage two, so we're adding it
# to the path here
import sys
sys.path.append('..')

from io_util import get_input_output_files


# Cleans a line of text according to the most common
# text readability errors found in the text documents
# (HTML tags and entities)
def clean_line(line):
    # Remove all HTML tags from the line
    line = re.sub(r'<[^>]+>', '', line)

    # Unescape from HTML
    line = html.unescape(line)

    return line


def main():
    # Define the input file encoding
    # and the output file name suffix,
    # so we can pass it into the method
    input_file_encoding = "cp1252"
    output_file_name_suffix = "cleaned"

    # Pass off boilerplate file processing
    # code to io_util.py
    files = get_input_output_files(input_file_encoding, output_file_name_suffix)

    # Grab files from the tuple
    input_file = files[0]
    output_file = files[1]

    # Read the input file line by line
    # (because there's a lot of lines and
    # loading them all would be inefficient)
    line = input_file.readline()

    # While there are still lines to read, clean them
    # and write them to the output file
    while line:
        output_file.write(clean_line(line))
        line = input_file.readline()

    # Close the files
    input_file.close()
    output_file.close()


if __name__ == "__main__":
    main()
