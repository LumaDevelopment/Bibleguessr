import os
import sys


# This method:
# 1) Grabs the input file name from the arguments
# 2) Creates the output file name by inserting the given suffix
#    before the file extension
# 3) Deletes the output file if it already exists
# 4) Opens the input and output files, then returns them
def get_input_output_files(input_file_encoding, output_file_name_suffix):
    # First, take the input file name as a command line argument
    input_file_name = sys.argv[1]

    # If no input file name is provided, print an error and return
    if not input_file_name or len(input_file_name) == 0:
        print("No input file name provided!")
        return None, None

    # Get the index of the start of the file extension
    extension_index = input_file_name.rfind(".")

    # If there is no extension, print an error and return
    if extension_index == -1:
        print("Input file has no extension!")
        return None, None

    # First, see if the input file name already has a
    # suffix. If it does, remove it.
    original_suffix = None
    suffix_index = input_file_name.rfind("_")
    if suffix_index != -1:
        original_suffix = input_file_name[suffix_index:extension_index]
        input_file_name = input_file_name[:suffix_index] + input_file_name[extension_index:]
        extension_index = input_file_name.rfind(".")

    # Create the output file name by inserting "_cleaned" before the extension
    if len(output_file_name_suffix) > 0:
        output_file_name = (input_file_name[:extension_index] +
                            "_" +
                            output_file_name_suffix +
                            input_file_name[extension_index:])
    else:
        output_file_name = input_file_name

    # Delete the file if it already exists
    if os.path.exists(output_file_name):
        os.remove(output_file_name)

    # Restore original suffix if there was one
    if original_suffix:
        input_file_name = input_file_name[:extension_index] + original_suffix + input_file_name[extension_index:]

    # Open the files. Input encoding is given as an argument
    # because in stage one the files are encoded with Windows
    # line terminators.
    input_file = open(input_file_name, "r", encoding=input_file_encoding)
    output_file = open(output_file_name, "w", encoding='utf-8')

    return input_file, output_file
