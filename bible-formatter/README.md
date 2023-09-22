# Bible Formatter

This folder details the process of finding Bible text files, cleaning them, and formatting them properly to be fed into
the Bible microservice. Includes the scripts to do so.

- Stage One: Convert from Windows line terminators to Unix line terminators, remove HTML tags and entities from the text
  files.
- Stage Two: Remove all non-verse lines from the text files, and format the verse lines with a reference + text format,
  using a custom (OSIS based) reference system. 