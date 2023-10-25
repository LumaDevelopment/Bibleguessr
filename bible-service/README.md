# Bible Service

## Service Features

- Can use multiple versions of the Bible, stored as text files. The user can tell the service where they have stored
  these files in a Bible Service configuration file.
- Stores Bible text in memory. Verse can be retrieved singularly or in large passages. Retrieval can be done by one of
  two references:
    1) **Universal Index** - The index of the verse in the Bible with a range of `[0, 31101]`
    2) **Book Index, Chapter Number, and Verse Number** - The index of the book (Genesis is 0, Revelation is 65), as
       well as the number of the chapter and verse. Example: Genesis 1:1 is `(0, 1, 1)`. We use numbers instead of
       strings because the names of the books of the Bible can vary by language and translation.
- Maintains a hierarchical, object-oriented representation of the Bible's books, chapters, and verses. These objects are
  comparable, and you can traverse through the Bible with them (ex. `Verse.nextVerse()` can take you from Genesis 1:1 to
  Genesis 1:2 or from Genesis 50:26 to Exodus 1:1).
- Allows a user to request a random verse from the Bible with a specified number of verses surrounding it to serve as
  context. The user specifies what version of the Bible the text will come from.
- Supports frontend operations by generating, maintaining, and serving a JSON object of service data. This includes the
  versions of the Bible the service has loaded, the names of every book of the Bible according to each version, and a 2D
  array which communicates the number of verses in every chapter of every book of the Bible.

## Class Structure & Descriptions

- `gg.bibleguessr.bible`
    - `gg.bibleguessr.bible.data_structures`
        - `Book` - Object-oriented representation of a book of the Bible. This object has methods to give you the number
          of chapters in the book, all `Chapter` objects in the book, the first `Chapter` object, the last `Chapter`
          object, and a specific `Chapter` object from the book. This object has a method to give you the object of the
          previous book of the Bible and the next book of the Bible. This object has a method which returns the book's
          index across all books of the Bible. This object has a method which returns whether this book is in the Old
          Testament and whether it is in the New Testament.
        - `Chapter` - Object-oriented representation of a chapter of the Bible. This object has methods to give you the
          number of verses in the chapter, all `Verse` objects in the chapter, the first `Verse` object, the `last`
          Verse object, and a specific `Verse` object from the chapter. This object has a method to give you the object
          of the previous chapter of the Bible and the next chapter of the Bible (regardless of whether it is in a
          different book). This object has a method which gives you the object of the book this chapter is in.
        - `Verse` - Object-oriented representation of a verse of the Bible. This object a method to give you the index
          of the verse across all verses of the Bible (called the **universal index**). This object has methods to give
          you the object of the previous verse of the Bible and the next verse of the Bible (regardless of whether it is
          in a different chapter or book), and the object of the chapter this verse is in.
        - `Version` - Represents a version of the Bible available to the Bible service. Contains the version's name and
          the name of every book of the Bible according to this version. Book names can be retrieved by `Book` object or
          book index.
    - `gg.bibleguessr.bible.requests`
        - `FrontendBibleDataMgr` - Creates, stores, and updates the data that the service responds with when it receives
          a frontend bible data request. This data is saved upon generation, and then updated accordingly when the
          versions available to the Bible service changes.
        - `FrontendBibleDataRequest` - Represents a frontend bible data request. To see a further description of the
          parameters for the request and how the Bible service responds to it, please
          see [RequestResponseSpecifications.md](RequestResponseSpecifications.md).
        - `RandomVerseRequest` - Represents a request for a random verse of the Bible. To see a further description of
          the parameters for the request and how the Bible service responds to it, please
          see [RequestResponseSpecifications.md](RequestResponseSpecifications.md).
    - `gg.bibleguessr.bible.versions`
        - `BibleVersionMgr` - Keeps track of all versions of the Bible available to the service, and notifies all
          interested classes when the versions change.
        - `VersionsUpdateListener` - An interface for classes to implement if they want to be notified
          when `BibleVersionMgr` updates the versions available to the service. Must be registered with
          the `BibleVersionMgr`.
    - `Bible` - The entry point to the hierarchical, object-oriented representation of the Bible's books, chapters, and
      verses. Implements the retrieval of `Book` objects by book index and the retrieval of `Verse` objects by both
      reference types listed above. Provides iterators for all the books and verses of the Bible.
    - `BibleService` - The core class of the Bible service. The Service Wrapper routes requests to this class.
      Additionally, this class keeps track of Bible files, configuration, accepted request types. This class holds one
      instance of each of the following classes: `BibleVersionMgr`, `BibleTextMgr`, and `FrontendBibleDataMgr`.
    - `BibleServiceConfig` - Configurable aspects of the Bible service. Serialized and deserialized to and from JSON
      with Jackson-Databind.
    - `BibleTextMgr` - Manages reading, storing, and providing the text of different versions of the Bible. This class
      allows the rest of the service to retrieve actual Bible text. Notifies `BibleVersionMgr` when it reads in a new
      version of the Bible. 