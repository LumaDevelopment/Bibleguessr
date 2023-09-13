# Bible Service

This service manages:

- The storage, retrieval, and reference of Bible passages.
- Managing Bible versions as maps from passage reference to text and retrieving from these versions efficiently without loading all versions into memory.
- Calculation of the proximity between two guesses.
- Generating valid, random Bible passage references and distributing the text of the passage in a specified version.

Notes:

- Create a RandomAccessFile (or other lang equivalent if not using Java) for each file, hold in a HashMap or something like that, then queue up seek() + read() operations because Bible Passage retrieval tasks could come from different threads. Might need to make a `BiblePassageReadRequest(Passage passage, Version version)` object or something like that.
