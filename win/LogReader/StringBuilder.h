#pragma once
#include <cstdlib>

class StringBuilderIterator;


struct StringBlock {
	StringBlock* next = nullptr;
	StringBlock* prev = nullptr;
	const char* str = nullptr;
	size_t strSize = 0;
	
	StringBlock(const char * str, size_t strSize);
	~StringBlock();

	char getChar(size_t strIndex) const;
	bool isValidIndex(size_t strIndex) const;
};


class String {
	char* value;
	size_t length;
public:
	String();
	String(const char* value);
	String(char* value, size_t length);
	String(const String& right);
	String& operator=(const String& right);
	~String();

	size_t getLength() const;
	const char* getValue() const;
};


class StringBuilderIterator {
    friend class StringBuilder;

    StringBlock* activeBlock;
    size_t activeBlockIndex;

    void init(StringBlock * block, size_t index);
    StringBuilderIterator(StringBlock* block, size_t index);
    bool checkOverflow(size_t offset) const;
    StringBlock* find(size_t & startIndex) const;
public:
    StringBuilderIterator operator+(size_t offset) const;
    StringBuilderIterator& operator++();
    char operator[](size_t offset) const;
    char operator*() const;

    bool hasCompleteLines() const;
    void skipLine();
    void skipAll();
};


class StringBuilder {
	StringBlock* first;
	StringBlock* last;
	size_t ignoreOffset;

	StringBuilder& operator=(const StringBuilder&) = delete;
	StringBuilder(const StringBuilder&) = delete;
	static char* extract(StringBuilderIterator fromIterator, size_t length);
public:
	StringBuilder();
	StringBuilder(const char* str);
	~StringBuilder();

	void append(const char* block);
	void append(const char* block, size_t blockSize);
	void clearBefore(const StringBuilderIterator& it);
	void clear();
	bool isEmpty();
	StringBuilderIterator begin() const;

	static String extractLine(const StringBuilderIterator & it);
	static String extractAll(const StringBuilderIterator & it);
	static size_t getLineOffset(StringBuilderIterator fromIterator);
	static size_t getOffset(StringBuilderIterator fromIterator, char target);
};

