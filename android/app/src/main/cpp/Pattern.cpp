#include "Pattern.h"
#include "Util.h"

PatternChain::PatternChain() {
	item = nullptr;
	next = nullptr;
}

PatternChain* PatternChain::add(PatternChain* target, PatternItem* item) {
	if (target == nullptr) {
		target = new PatternChain();
	}
	target->item = item;
	return target;
}

PatternChain::~PatternChain() {
	delete item;
	delete next;
}

bool PatternChain::match(const char* str) const {
	StringBuilder sb(str);
	return item->match(this, sb.begin());
}

bool PatternChain::match(const StringBuilderIterator & it) const {
	return item->match(this, it);
}

PatternItem** PatternChain::createItems(const char* correctMask, const size_t size) {
	if (size == 0) {
		return nullptr;
	}

	auto** result = new PatternItem*[size];
	for (size_t i = 0; i < size; i++) {
		result[i] = PatternItem::create(correctMask[i]);
	}

	return result;
}

PatternChain* PatternChain::createChain(const char* correctMask, const size_t size) {
	if (size == 0) {
		return nullptr;
	}

	const auto pattern = createItems(correctMask, size);
	auto* result = new PatternChain();
	auto* ptr = result;
	for(size_t i = 0; i < size; i++) {
		ptr->item = pattern[i];

		const bool notLastLoop = (i + 1 != size);
		if (notLastLoop) {
			ptr->next = new PatternChain();
			ptr = ptr->next;
		}
	}
	delete[] pattern;
	return result;
}

const size_t Pattern::MAX_PATTERN_LENGTH = 1024;
Pattern Pattern::oneLine = Pattern("*\n");

void Pattern::init(const char* mask) {
	correctMask = createCorrectedMask(mask);

	if (correctMask == nullptr) {
		state = PatternState::BAD_MASK;
		chain = nullptr;
		return;
	}
	
	const auto size = util::strlen(correctMask);
	if (size > MAX_PATTERN_LENGTH) {
		state = PatternState::TOO_BIG_PATTERN;
		chain = nullptr;
		return;
	}
	
	state = PatternState::READY_TO_WORK;
	chain = PatternChain::createChain(correctMask, size);
}

Pattern::Pattern() {
	state = PatternState::NOT_INITIALIZED;
	correctMask = nullptr;
	chain = nullptr;
}

Pattern::Pattern(const char* mask) 
: state(PatternState::NOT_INITIALIZED)
, correctMask(nullptr)
, chain(nullptr) {
	init(mask);
}

Pattern::Pattern(const Pattern& right) 
: state(PatternState::NOT_INITIALIZED)
, correctMask(nullptr)
, chain(nullptr) {
	init(right.correctMask);
}

Pattern::~Pattern() {
	delete[] correctMask;
	delete chain;
}

Pattern& Pattern::operator=(const Pattern & right) {
	if (&right == this) {
		return *this;
	}

	delete chain;
	delete[] correctMask;

	init(right.correctMask);
	
	return *this;
}

bool Pattern::hasErrors() const {
	return state != PatternState::READY_TO_WORK;
}

bool Pattern::match(const char* str) const {
	if (hasErrors()) {
		return false;
	}

	StringBuilder sb(str);
	return chain->match(sb.begin());
}

bool Pattern::match(const StringBuilderIterator& it) const {
	if (hasErrors()) {
		return false;
	}
	return chain->match(it);
}

size_t Pattern::getCorrectedMaskLength(const char* mask) {
	if (mask == nullptr) {
		return 0;
	}

	size_t result = 0;
	bool isStarPrevious = false;

	for (int i = 0; mask[i]; i++) {
		const char c = mask[i];
		if (!isMaskSymbol(c)) {
			result = 0;
			break;
		}

		if (c == '*') {
			if (isStarPrevious) continue;
			isStarPrevious = true;
		}
		else {
			isStarPrevious = false;
		}

		result++;
	}
	return result;
}

char* Pattern::createCorrectedMask(const char* mask) {
	/* Removing double stars. Not obvious. */

	const auto length = getCorrectedMaskLength(mask);
	if (length == 0) {
		return nullptr;
	}

	auto* result = new char[length + 1];
	result[length] = 0;
	size_t index = 0;
	bool isStarPrevious = false;
	for (int i = 0; mask[i]; i++) {
		const char c = mask[i];

		if (c == '*') {
			if (isStarPrevious) continue;
			isStarPrevious = true;
		}
		else {
			isStarPrevious = false;
		}

		result[index] = c;
		index++;
	}

	return result;
}

bool Pattern::isMaskSymbol(const char c) {
	if (c == '\0') {
		return false;
	}

	return c == '?' || c == '*' || !util::isLineEnding(c);// util::isLetterOrDigit(c);
}

bool PatternItem::canContinueParse(const char c) {
	return c != '\0' && c != '\r' && c != '\n';
}

bool PatternItem::canNotContinueParse(const char c) {
	return !(canContinueParse(c));
}

PatternItem* PatternItem::create(const char c) {
	if (c == '*') return new AnySymbol;
	if (c == '?') return new OneUnknownSymbol;
	return new CurrentSymbol(c);
}

CurrentSymbol::CurrentSymbol(const char c) {
	waitSymbol = c;
}

bool CurrentSymbol::waitFor(const char c) const {
	return waitSymbol == c;
}

bool CurrentSymbol::match(const PatternChain* chain, const StringBuilderIterator& it) const {
	const char currentSymbol = it[0];
	if (waitSymbol != currentSymbol) {
		return false;
	}

	if (chain->next == nullptr) {
		return canNotContinueParse(it[1]);
	}

	return chain->next->match(it + 1);
}

bool AnySymbol::match(const PatternChain* chain, const StringBuilderIterator& it) const {
    if (chain->next == nullptr) {
		return true;
	}

	size_t index = 0;
	while (canContinueParse(it[index])) {
		const bool found = chain->next->match(it + index);
		if (found) {
			return true;
		}
		index++;
	}
	return false;
}

bool OneUnknownSymbol::match(const PatternChain* chain, const StringBuilderIterator& it) const {
	if (chain->next == nullptr) {
		return canContinueParse(it[0]) &&
			canNotContinueParse(it[1]);
	}

	if (canNotContinueParse(it[0])) {
		return false;
	}

	return chain->next->match(it + 1);
}