## LogReader

This is small regex engine, written on C++ and used in Android application. It is test task from one of my employer, which i had successfully done.

Program downloads ANSI-text file by URL address, parses it with regular expression, shows matched lines and supports the following features:
- Remember search history
- Allow to select and copy to clipboard matched lines
- Write internal log file with matches
- Saves parse progress while screen rotating

Restrictions
- Program must work with large files over 100mb
- Do not use Regex
- Do not use STL in C++ part
- Native log reader must implement following interface:
  ``` cpp
  class CLogReader { 
  public:
	bool setFilter(const char* filter);
	bool addSourceBlock(const char* block, size_t size);
  }
  ```

#### Regex support

- '*' - any symbol sequence or nothing
- '?' - any one symbol
- A-Z, a-z, 0-9, etc... any ANSI symbols

Example: regex "?asd*" find matches on "wasd123" string.

#### Structure
Project contains two parts: Java application for Android and C++ program for Windows. Android app asynchronously load resources and transfer data blocks through JNI to C++ engine for parsing. Win-part contains unit tests for C++ engine. Such split allows fast dev-test cycle for native part.

#### Coding time
I've spent around 40 hours for write it. No magic. Only this approach works: think-code-test-clean-think... =)

