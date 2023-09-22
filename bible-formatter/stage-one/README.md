# Stage One

The first task is to get a plaintext file for each public domain version of the Bible we're targeting. At first, I was
gathering these files independently. Then, I came across <a href="http://biblehub.net/">this site</a> which has the
following versions in (1) plain text and (2) standardized format:

- American Standard Version (ASV)
- Darby Bible Translation (DBT)
- English Revised Version (ERV)
- King James Version (KJV)
- Young's Literal Translation (YLT)

So, this made my life significantly easier. Thanks to the team behind BibleHub! The only problem remaining is that these
files have HTML formatting and use Windows line termination. So, I made a script to remove the formatting and convert
the line terminators to Unix style.