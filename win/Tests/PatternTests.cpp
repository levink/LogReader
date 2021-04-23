#include "gtest/gtest.h"
#include "Pattern.h"

struct CreateFromCorrectPatternTest {
	PatternItem** filter;
	size_t size;
	CreateFromCorrectPatternTest(const char* pattern, const size_t size) {
		this->size = size;
		this->filter = PatternChain::createItems(pattern, size);
	}
	~CreateFromCorrectPatternTest() {

		if (filter == nullptr) {
			return;
		}

		for (size_t i = 0; i < size; i++) {
			delete filter[i];
		}

		delete[] filter;
	}
};

TEST(Mask, createFromCorrectPattern1) {
	const auto test = CreateFromCorrectPatternTest("*", 1);
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[0]));
}

TEST(Mask, createFromCorrectPattern2) {
	const auto test = CreateFromCorrectPatternTest("*asd*", 5);
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[0]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[1]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[2]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[3]));
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[4]));

	ASSERT_TRUE(((CurrentSymbol*)test.filter[1])->waitFor('a'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[2])->waitFor('s'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[3])->waitFor('d'));
}

TEST(Mask, createFromCorrectPattern3) {
	const auto test = CreateFromCorrectPatternTest("*a?d*?", 6);
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[0]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[1]));
	ASSERT_EQ(typeid(OneUnknownSymbol), typeid(*test.filter[2]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[3]));
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[4]));
	ASSERT_EQ(typeid(OneUnknownSymbol), typeid(*test.filter[5]));

	ASSERT_TRUE(((CurrentSymbol*)test.filter[1])->waitFor('a'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[3])->waitFor('d'));
}

TEST(Mask, createFromCorrectPattern4) {
	const auto test = CreateFromCorrectPatternTest("a?d*a*s*d", 9);

	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[0]));
	ASSERT_EQ(typeid(OneUnknownSymbol), typeid(*test.filter[1]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[2]));
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[3]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[4]));
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[5]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[6]));
	ASSERT_EQ(typeid(AnySymbol), typeid(*test.filter[7]));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*test.filter[8]));


	ASSERT_TRUE(((CurrentSymbol*)test.filter[0])->waitFor('a'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[2])->waitFor('d'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[4])->waitFor('a'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[6])->waitFor('s'));
	ASSERT_TRUE(((CurrentSymbol*)test.filter[8])->waitFor('d'));
}

TEST(Chain, createFromCorrect1) {
	const auto* chain = PatternChain::createChain("*", 1);
	ASSERT_NE(nullptr, chain);
	ASSERT_EQ(nullptr, chain->next);
	ASSERT_EQ(typeid(AnySymbol), typeid(*chain->item));
	delete chain;
}

TEST(Chain, createFromCorrect2) {
	const auto* chain = PatternChain::createChain("?", 1);
	ASSERT_NE(nullptr, chain);
	ASSERT_EQ(nullptr, chain->next);
	ASSERT_EQ(typeid(OneUnknownSymbol), typeid(*chain->item));
	delete chain;
}

TEST(Chain, createFromCorrect3) {
	const auto* chain = PatternChain::createChain("a*", 2);
	ASSERT_NE(nullptr, chain);
	ASSERT_NE(nullptr, chain->next);
	ASSERT_EQ(nullptr, chain->next->next);

	ASSERT_EQ(typeid(CurrentSymbol), typeid(*chain->item));
	ASSERT_TRUE(((CurrentSymbol*)chain->item)->waitFor('a'));
	ASSERT_EQ(typeid(AnySymbol), typeid(*chain->next->item));

	delete chain;
}

TEST(Chain, createFromCorrect4) {
	const auto* chain = PatternChain::createChain("*a?", 3);
	ASSERT_NE(nullptr, chain);
	ASSERT_NE(nullptr, chain->next);
	ASSERT_NE(nullptr, chain->next->next);
	ASSERT_EQ(nullptr, chain->next->next->next);

	ASSERT_EQ(typeid(AnySymbol), typeid(*chain->item));
	ASSERT_EQ(typeid(CurrentSymbol), typeid(*chain->next->item));
	ASSERT_EQ(typeid(OneUnknownSymbol), typeid(*chain->next->next->item));
	ASSERT_TRUE(((CurrentSymbol*)chain->next->item)->waitFor('a'));

	delete chain;
}

TEST(MatchCorrectMask, EndsWithLetter) {
	const auto* chain = PatternChain::createChain("*a", 3);
	ASSERT_TRUE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_FALSE(chain->match("asd"));
	ASSERT_TRUE(chain->match("aasda"));
	ASSERT_FALSE(chain->match("aaaaaab"));
	delete chain;
}

TEST(MatchCorrectMask, EndsWithSequence) {
	const auto* chain = PatternChain::createChain("*asd", 4);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_TRUE(chain->match("asd"));
	ASSERT_TRUE(chain->match("asdasd"));
	ASSERT_TRUE(chain->match("aaaasd"));
	ASSERT_FALSE(chain->match("aaaasdas"));
	ASSERT_FALSE(chain->match("aaaasda"));
	delete chain;
}

TEST(MatchCorrectMask, EndsWithUnknown1) {
	const auto* chain = PatternChain::createChain("*a?", 3);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_TRUE(chain->match("ad"));
	ASSERT_FALSE(chain->match("asd"));
	ASSERT_TRUE(chain->match("aaaad"));
	ASSERT_FALSE(chain->match("aaaada"));
	ASSERT_FALSE(chain->match("aaaadd"));
	delete chain;
}

TEST(MatchCorrectMask, EndsWithUnknown2) {
	const auto* chain = PatternChain::createChain("a*?", 3);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_TRUE(chain->match("ad"));
	ASSERT_TRUE(chain->match("asd"));
	ASSERT_TRUE(chain->match("assssd"));
	ASSERT_TRUE(chain->match("asdsdsd"));
	ASSERT_FALSE(chain->match("sasdsdsd"));
	delete chain;
}

TEST(MatchCorrectMask, UnknownInTheMiddleOfThree) {
	const auto* chain = PatternChain::createChain("a?a", 3);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("ad"));
	ASSERT_FALSE(chain->match("add"));
	ASSERT_FALSE(chain->match("aa"));
	ASSERT_TRUE(chain->match("aaa"));
	ASSERT_TRUE(chain->match("ada"));
	ASSERT_FALSE(chain->match("asda"));
	delete chain;
}

TEST(MatchCorrectMask, StartsWithLetter) {
	const auto* chain = PatternChain::createChain("a*", 2);
	ASSERT_TRUE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_TRUE(chain->match("asd"));
	ASSERT_TRUE(chain->match("asdasd"));
	ASSERT_TRUE(chain->match("aaaasd"));
	ASSERT_FALSE(chain->match("baaasdas"));
	ASSERT_FALSE(chain->match("baaasda"));
	delete chain;
}

TEST(MatchCorrectMask, StartsWithSequence1) {
	const auto* chain = PatternChain::createChain("asd*", 4);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_TRUE(chain->match("asd"));
	ASSERT_TRUE(chain->match("asdasd"));
	ASSERT_TRUE(chain->match("asdaaaasd"));
	ASSERT_FALSE(chain->match("baaasdas"));
	ASSERT_FALSE(chain->match("baaasda"));
	delete chain;
}

TEST(MatchCorrectMask, StartsWithSequence2) {
	const auto* chain = PatternChain::createChain("asd??*", 6);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_FALSE(chain->match("asd"));
	ASSERT_TRUE(chain->match("asdasd"));
	ASSERT_TRUE(chain->match("asdaaaasd"));
	ASSERT_FALSE(chain->match("baaasdas"));
	ASSERT_FALSE(chain->match("baaasda"));
	delete chain;
}

TEST(MatchCorrectMask, StartsWithSequenceAndUnknownEnd) {
	const auto* chain = PatternChain::createChain("asd???", 6);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_FALSE(chain->match("asd"));
	ASSERT_TRUE(chain->match("asdasd"));
	ASSERT_TRUE(chain->match("asdaaa"));
	ASSERT_FALSE(chain->match("asdaaaa"));
	delete chain;
}

TEST(MatchCorrectMask, StartsAndEndsWithLetter) {
	const auto* chain = PatternChain::createChain("a*d", 3);
	ASSERT_FALSE(chain->match("a"));
	ASSERT_FALSE(chain->match("b"));
	ASSERT_TRUE(chain->match("asd"));
	ASSERT_TRUE(chain->match("assd"));
	ASSERT_TRUE(chain->match("aaadddd"));
	ASSERT_FALSE(chain->match("asda"));
	ASSERT_FALSE(chain->match("dasd"));
	delete chain;
}

TEST(Pattern, GetMaskLength) {
	ASSERT_EQ(0, Pattern::getCorrectedMaskLength(nullptr));
	ASSERT_EQ(1, Pattern::getCorrectedMaskLength("a"));
	ASSERT_EQ(2, Pattern::getCorrectedMaskLength("as"));
	ASSERT_EQ(2, Pattern::getCorrectedMaskLength("a*"));
	ASSERT_EQ(3, Pattern::getCorrectedMaskLength("*a*"));
	ASSERT_EQ(4, Pattern::getCorrectedMaskLength("*a?*"));
	ASSERT_EQ(4, Pattern::getCorrectedMaskLength("*a*?"));
	ASSERT_EQ(2, Pattern::getCorrectedMaskLength("*?"));
	ASSERT_EQ(3, Pattern::getCorrectedMaskLength("?a*"));
	ASSERT_EQ(6, Pattern::getCorrectedMaskLength("????a***"));
	ASSERT_EQ(1, Pattern::getCorrectedMaskLength("**"));
	ASSERT_EQ(1, Pattern::getCorrectedMaskLength("*"));
	ASSERT_EQ(1, Pattern::getCorrectedMaskLength("***"));
	ASSERT_EQ(3, Pattern::getCorrectedMaskLength("*a**"));
	ASSERT_EQ(0, Pattern::getCorrectedMaskLength(""));
	ASSERT_EQ(1, Pattern::getCorrectedMaskLength(" "));
	ASSERT_EQ(4, Pattern::getCorrectedMaskLength("asd "));
	ASSERT_EQ(3, Pattern::getCorrectedMaskLength("? ?"));
	ASSERT_EQ(2, Pattern::getCorrectedMaskLength("**a"));
	ASSERT_EQ(1, Pattern::getCorrectedMaskLength("************"));
}

void isEqual(const char* expected, const char* mask) {
	char* createdMask = Pattern::createCorrectedMask(mask);
	EXPECT_STREQ(expected, createdMask);
	delete[] createdMask;
}

TEST(Pattern, CorrectMask) {
	isEqual("a", "a");
	isEqual("asd", "asd");
	isEqual("*a", "**a");
	isEqual("*a*", "**a*");
	isEqual("*a*", "**a*");
	isEqual("*a*", "**a**");
	isEqual("*a*a*", "**a****a****");
	isEqual("a??", "a??");
	isEqual("as?d", "as?d");
	isEqual("*?*a", "**?***a");
	isEqual("?*a", "?*****a");
	isEqual("*a??*a*", "**a??****a****");
}

TEST(Pattern, Create) {
	ASSERT_FALSE(Pattern("asd?").hasErrors());
	ASSERT_FALSE(Pattern("*").hasErrors());
	ASSERT_FALSE(Pattern("*?").hasErrors());
	ASSERT_FALSE(Pattern("*?**").hasErrors());
	ASSERT_FALSE(Pattern("*a").hasErrors());
	ASSERT_FALSE(Pattern("a*").hasErrors());
	ASSERT_FALSE(Pattern("***").hasErrors());
	ASSERT_FALSE(Pattern("*a*s***d***").hasErrors());
	ASSERT_FALSE(Pattern(" ").hasErrors());
	ASSERT_FALSE(Pattern("asd ").hasErrors());
	ASSERT_FALSE(Pattern("1 asd").hasErrors());
	ASSERT_FALSE(Pattern(" asd").hasErrors());

	ASSERT_TRUE(Pattern(nullptr).hasErrors());
	ASSERT_TRUE(Pattern("").hasErrors());
	ASSERT_TRUE(Pattern("\nasd").hasErrors());
	ASSERT_TRUE(Pattern("asd\n").hasErrors());
}

TEST(Pattern, TestClassSafety) {
	Pattern p;
	ASSERT_TRUE(p.hasErrors());

	p = Pattern("a");
	ASSERT_FALSE(p.hasErrors());
	ASSERT_TRUE(p.match("a"));
	ASSERT_FALSE(p.match("b"));
	ASSERT_FALSE(p.match("ab"));
	ASSERT_FALSE(p.match("ba"));

	p = Pattern("b");
	ASSERT_FALSE(p.hasErrors());
	ASSERT_TRUE(p.match("b"));
	ASSERT_FALSE(p.match("a"));
	ASSERT_FALSE(p.match("ab"));
	ASSERT_FALSE(p.match("ba"));

	Pattern p2(p);
	ASSERT_FALSE(p2.hasErrors());
	ASSERT_TRUE(p2.match("b"));
	ASSERT_FALSE(p2.match("a"));
	ASSERT_FALSE(p2.match("ab"));
	ASSERT_FALSE(p2.match("ba"));
}

TEST(Pattern, MatchOneLine1) {
	auto p = Pattern("***asd**a*s");
	ASSERT_FALSE(p.hasErrors());
	ASSERT_TRUE(p.match("asdas"));
	ASSERT_FALSE(p.match("asdasd"));
	ASSERT_FALSE(p.match("adsdas"));
	ASSERT_TRUE(p.match("sasdas"));
	ASSERT_TRUE(p.match("asdaas"));
	ASSERT_TRUE(p.match("asdaaas"));
	ASSERT_TRUE(p.match("asdaaafs"));
	ASSERT_TRUE(p.match("ffasdfffffafs"));
}

TEST(Pattern, MatchOneLine2) {
	auto p = Pattern("a***s***********");
	ASSERT_FALSE(p.hasErrors());
	ASSERT_TRUE(p.match("as"));
	ASSERT_FALSE(p.match("sas"));
	ASSERT_FALSE(p.match("aaaa"));
	ASSERT_TRUE(p.match("aaas"));
	ASSERT_TRUE(p.match("aaasss"));
}

TEST(Pattern, MatchOneLine3) {
	auto p = Pattern("**a***s***********?");
	ASSERT_FALSE(p.hasErrors());
	ASSERT_TRUE(p.match("asa"));
	ASSERT_TRUE(p.match("asaaa"));
	ASSERT_TRUE(p.match("aaasa"));
	ASSERT_FALSE(p.match("a"));
	ASSERT_FALSE(p.match("sas"));
	ASSERT_FALSE(p.match("aaaa"));
	ASSERT_FALSE(p.match("aaaaaas"));
	ASSERT_TRUE(p.match("aaasss"));
}

TEST(Pattern, MatchInFirstLine) {
	auto p = Pattern("asd?");
	ASSERT_FALSE(p.hasErrors());
	ASSERT_FALSE(p.match("aa\nasdfg"));
	ASSERT_FALSE(p.match("asdfg\nasdfg"));
	ASSERT_FALSE(p.match("\nasdfg"));
	ASSERT_FALSE(p.match("\n\n\nasdfg"));
	ASSERT_FALSE(p.match("\r\n\nasdfg"));
	ASSERT_TRUE(p.match("asdf\nasdfg"));
	ASSERT_TRUE(p.match("asdf\r\nasdfg"));
	ASSERT_TRUE(p.match("asdf\n\n\n\nasdfg"));
}