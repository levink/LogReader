#pragma once
#include "StringBuilder.h"

class PatternItem;

class PatternChain {
	PatternChain();
	PatternChain(const PatternChain & item) = default;
	PatternChain& operator=(const PatternChain & item) = delete;
	static PatternChain* add(PatternChain* target, PatternItem* item);
public:
	const PatternItem* item;
	PatternChain* next;
	~PatternChain();
	bool match(const char * str) const;
	bool match(const StringBuilderIterator & it) const;

	static PatternItem** createItems(const char* correctMask, size_t size);
	static PatternChain* createChain(const char* correctMask, size_t size);
};

struct PatternState {
	enum State {
		READY_TO_WORK	= 0,
		NOT_INITIALIZED = 1,
		BAD_MASK		= 2,
		TOO_BIG_PATTERN	= 3
	};
};

class Pattern {
	static const size_t MAX_PATTERN_LENGTH;

	PatternState::State state;
	char* correctMask;
	PatternChain* chain;
	
	void init(const char* mask);
public:

	Pattern();
	Pattern(const char* mask);
	Pattern(const Pattern & right);
	~Pattern();
	Pattern& operator=(const Pattern & right);
	bool hasErrors() const;
	bool match(const char* str) const;
	bool match(const StringBuilderIterator& it) const;

	static size_t getCorrectedMaskLength(const char* mask);
	static char* createCorrectedMask(const char* mask);
	static bool isMaskSymbol(char c);
	static Pattern oneLine;
};

class PatternItem {
protected:
	static bool canContinueParse(char c);
	static bool canNotContinueParse(char c);
public:
	static PatternItem* create(char c);
	
	virtual ~PatternItem() = default;
	virtual bool match(const PatternChain * chain, const StringBuilderIterator & it) const = 0;
};

class CurrentSymbol : public PatternItem {
	char waitSymbol;
public:
	CurrentSymbol(char c);
	bool waitFor(char c) const;
	bool match(const PatternChain* chain, const StringBuilderIterator& it) const override;
};

class AnySymbol : public PatternItem {
public:
	bool match(const PatternChain* chain, const StringBuilderIterator& it) const override;
};

class OneUnknownSymbol : public PatternItem {
public:
	bool match(const PatternChain* chain, const StringBuilderIterator& it) const override;
};




