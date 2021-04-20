#include "StringBuilder.h"
#include "Util.h"
#include "Pattern.h"

StringBlock::StringBlock(const char* str, const size_t strSize) {
	next = nullptr;
	prev = nullptr;
	this->str = util::createCopy(str, strSize);
	this->strSize = strSize;
}

StringBlock::~StringBlock() {
	delete[] str;
}

char StringBlock::getChar(const size_t strIndex) const {
	if (isValidIndex(strIndex)) {
		return str[strIndex];
	}
	return 0;
}

bool StringBlock::isValidIndex(const size_t strIndex) const {
	return strSize > 0 && strSize > strIndex;
}



String::String() {
	value = nullptr;
	length = 0;
}

String::String(char* value, const size_t length) {
	this->length = length;
	this->value = value;
}

String::String(const char* value) {
	length = util::strlen(value);
	this->value = util::createCopy(value, length);
}

String::String(const String& right) {
	length = right.length;
	value = util::createCopy(right.value, length);
}

String& String::operator=(const String& right) {
	if (this != &right) {
		delete[] value;
		length = right.length;
		value = util::createCopy(right.value, length);
	}
	return *this;
}

String::~String() {
	delete[] value;
}

size_t String::getLength() const {
	return length;
}

const char* String::getValue() const {
	return value;
}



StringBuilder::StringBuilder() {
	first = nullptr;
	last = nullptr;
	ignoreOffset = 0;
}

StringBuilder::StringBuilder(const char* str) {
	first = nullptr;
	last = nullptr;
	ignoreOffset = 0;
	const auto size = util::strlen(str);
	append(str, size);
}

StringBuilder::~StringBuilder() {
	clear();
}

void StringBuilder::append(const char* block) {
	const auto size = util::strlen(block);
	append(block, size);
}

void StringBuilder::append(const char* block, const size_t blockSize) {
	if (first == nullptr) {
		first = new StringBlock(block, blockSize);
		last = first;
	}
	else {
		auto* item = new StringBlock(block, blockSize);
		last->next = item;
		item->prev = last;
		last = last->next;
	}
}

void StringBuilder::clearBefore(const StringBuilderIterator& it) {
	
	if (it.activeBlock == nullptr) {
		clear();
		return;
	}

	ignoreOffset = it.activeBlockIndex;
	first = it.activeBlock;
    if (!first->prev) {
        return;
    }

	auto start = it.activeBlock->prev;
	start->next->prev = nullptr;

	while(start) {
		const auto tmp = start;
		start = start->prev;
		tmp->next = nullptr;
		tmp->prev = nullptr;
		delete tmp;
	}
}

void StringBuilder::clear() {
	StringBlock* activeBlock = first;
	while (activeBlock) {
		auto* tmp = activeBlock;
		activeBlock = activeBlock->next;
		delete tmp;
	}
	first = nullptr;
	last = nullptr;
	ignoreOffset = 0;
}

StringBuilderIterator StringBuilder::begin() const {
	return { this->first, ignoreOffset };
}

bool StringBuilderIterator::hasCompleteLines() const {

    auto it = *this;
    char c = *it;
    while (c) {
        if (util::isLineEnding(c)) {
            return true;
        }
        ++it;
        c = *it;
    }

    return false;
}

void StringBuilderIterator::skipLine() {
	
	const auto & self = *this;
	const auto lineLength = StringBuilder::getLineOffset(self);

	size_t offset = lineLength;
	if (util::isLineEnding(self[lineLength])) offset++;
	if (util::isLineEnding(self[lineLength + 1])) offset++;

    if (offset == 0) {
        return;
    }
	init(activeBlock, activeBlockIndex + offset);
}

void StringBuilderIterator::skipAll() {
	const auto & self = *this;
	const auto offset = StringBuilder::getOffset(self, '\0');
	if (offset == 0) {
		return;
	}
	init(activeBlock, activeBlockIndex + offset);
}

String StringBuilder::extractLine(const StringBuilderIterator & it) {
	const size_t length = getLineOffset(it);
	char* value = extract(it, length);
	return String(value, length);
}

String StringBuilder::extractAll(const StringBuilderIterator & it) {
	const size_t length = getOffset(it, '\0');
	char* value = extract(it, length);
	return String(value, length);
}

size_t StringBuilder::getLineOffset(StringBuilderIterator fromIterator) {
	size_t offset = 0;
	char c = *fromIterator;
	while (!util::isLineEnding(c)) {
		if (!c) {
			return 0;
		}
		++fromIterator;
		c = *fromIterator;
		offset++;
	}
	return offset;
}

size_t StringBuilder::getOffset(StringBuilderIterator fromIterator, const char target) {
	size_t offset = 0;
	char c = *fromIterator;
	while (c != target) {
		if (!c) {
			return 0;
		}
		++fromIterator;
		c = *fromIterator;
		offset++;
	}
	return offset;
}

char* StringBuilder::extract(StringBuilderIterator fromIterator, const size_t length) {
	auto* result = new char[length + 1];
	for(size_t i = 0; i < length; i++) {
		result[i] = *fromIterator;
		++fromIterator;
	}
	result[length] = 0;
	return result;
}

bool StringBuilder::isEmpty() {
    return first == nullptr;
}

void StringBuilderIterator::init(StringBlock* block, size_t index) {
	while (block && !block->isValidIndex(index)) {
		index -= block->strSize;
		block = block->next;
	}
	activeBlock = block;
	activeBlockIndex = index;
}

StringBuilderIterator::StringBuilderIterator(StringBlock* block, const size_t index) {
	activeBlock = nullptr;
	activeBlockIndex = 0;
	init(block, index);
}

bool StringBuilderIterator::checkOverflow(const size_t offset) const {
	return activeBlockIndex + offset < activeBlockIndex;
}

StringBlock* StringBuilderIterator::find(size_t& startIndex) const {
	auto block = activeBlock;
	while (block && !block->isValidIndex(startIndex)) {
		startIndex -= block->strSize;
		block = block->next;
	}
	return block;
}

char StringBuilderIterator::operator[](const size_t offset) const {
	if (checkOverflow(offset)) {
		return 0;
	}

	auto index = activeBlockIndex + offset;
	const auto block = find(index);
	if (block == nullptr) {
		return 0;
	}

	return block->getChar(index);
}

char StringBuilderIterator::operator*() const {
	if (activeBlock == nullptr) {
		return 0;
	}
	return activeBlock->getChar(activeBlockIndex);
}

StringBuilderIterator& StringBuilderIterator::operator++() {
	if (activeBlock == nullptr) {
		return *this;
	}

	const char current = activeBlock->getChar(activeBlockIndex);
	if (!current) {
		return *this;
	}
	
	init(activeBlock, activeBlockIndex + 1);
	return *this;
}

StringBuilderIterator StringBuilderIterator::operator+(const size_t offset) const{
	return { activeBlock, activeBlockIndex + offset };
}
