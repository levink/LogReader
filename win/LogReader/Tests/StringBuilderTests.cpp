#include "gtest/gtest.h"
#include "StringBuilder.h"

TEST(StringBuilder, CreateEmpty) {
	StringBuilder sb;
	const auto str = sb.begin();
	ASSERT_EQ('\0', str[0]);
	ASSERT_EQ('\0', str[1]);
	ASSERT_EQ('\0', str[100]);
}

TEST(StringBuilder, AddEmpty) {
	StringBuilder sb;
	sb.append("");
	const auto str = sb.begin();
	ASSERT_EQ('\0', str[0]);
	ASSERT_EQ('\0', str[1]);
	ASSERT_EQ('\0', str[100]);
}

TEST(StringBuilder, AddNull) {
	StringBuilder sb;
	sb.append(nullptr);
	const auto str = sb.begin();
	ASSERT_EQ('\0', str[0]);
	ASSERT_EQ('\0', str[1]);
	ASSERT_EQ('\0', str[200]);
}

TEST(StringBuilder, AddSomeGood) {
	StringBuilder sb;
	sb.append("0");
	sb.append("12");
	const auto str = sb.begin();
	ASSERT_EQ('0', str[0]);
	ASSERT_EQ('0', str[0]);
	ASSERT_EQ('0', str[0]);
	ASSERT_EQ('1', str[1]);
	ASSERT_EQ('1', str[1]);
	ASSERT_EQ('1', str[1]);
	ASSERT_EQ('1', str[1]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ(0, str[3]);
}

TEST(StringBuilder, AddSomeBad) {
	StringBuilder sb;
	sb.append(nullptr);
	sb.append("123");
	sb.append("");
	sb.append("4");
	sb.append(nullptr);
	const auto str = sb.begin();
	ASSERT_EQ('1', str[0]);
	ASSERT_EQ('2', str[1]);
	ASSERT_EQ('3', str[2]);
	ASSERT_EQ('4', str[3]);
	ASSERT_EQ('\0', str[5]);
	ASSERT_EQ('\0', str[6]);
	ASSERT_EQ('\0', str[200]);
}

TEST(StringBuilder, NegativeIndex) {
	StringBuilder sb;
	sb.append("0123");
	const auto str = sb.begin();
	ASSERT_EQ('0', str[0]);
	ASSERT_EQ('1', str[1]);
	ASSERT_EQ('2', str[2]);
	ASSERT_EQ('3', str[3]);
	ASSERT_EQ('\0', str[4]);
	ASSERT_EQ('\0', str[-1]);
	ASSERT_EQ('\0', str[-2]);
	ASSERT_EQ('\0', str[-3]);
}

TEST(StringBuilder, NegativeIndexWithOffset) {
	StringBuilder sb;
	sb.append("0123");
	const auto str = sb.begin() + 1;
	ASSERT_EQ('1', str[0]);
	ASSERT_EQ('2', str[1]);
	ASSERT_EQ('3', str[2]);
	ASSERT_EQ('\0', str[3]);
	ASSERT_EQ('\0', str[4]);
	ASSERT_EQ('\0', str[-1]);
	ASSERT_EQ('\0', str[-2]);
	ASSERT_EQ('\0', str[-3]);
}

TEST(StringBuilder, OffsetGood) {
	StringBuilder sb;
	sb.append("012");
	sb.append("3456");
	const auto str = sb.begin() + 5;
	ASSERT_EQ('5', str[0]);
}

TEST(StringBuilder, OffsetBig) {
	StringBuilder sb;
	sb.append("012");
	sb.append("3456");
	const auto str = sb.begin() + 500;
	ASSERT_EQ('\0', str[0]);
}

TEST(StringBuilder, OffsetNegative) {
	StringBuilder sb("012");
	sb.append("3456");
	const auto str = sb.begin() + 500;
	ASSERT_EQ('\0', str[0]);
}

TEST(StringBuilder, hasCompleteLines) {
	StringBuilder sb("012");
	;
	ASSERT_FALSE(sb.begin().hasCompleteLines());

	sb.append("345");
	ASSERT_FALSE(sb.begin().hasCompleteLines());

	sb.append("b\n123");
	ASSERT_TRUE(sb.begin().hasCompleteLines());
}

TEST(StringBuilder, extractFromOneLine) {
	StringBuilder sb("012");
	sb.append("34");
	sb.append("5");

	auto result = StringBuilder::extractLine(sb.begin());
	ASSERT_EQ(0, result.getLength());

	result = StringBuilder::extractAll(sb.begin());
	ASSERT_EQ(6, result.getLength());
	ASSERT_STREQ("012345", result.getValue());
}

TEST(StringBuilder, ExtractFromTwoLines) {
	StringBuilder sb("012");
	sb.append("34");
	sb.append("5\naaa");
	auto result = StringBuilder::extractLine(sb.begin());
	ASSERT_EQ(6, result.getLength());
	ASSERT_STREQ("012345", result.getValue());
}

TEST(StringBuilder, ExtractWithCRLFLineEnding) {
	StringBuilder sb("012");
	sb.append("34\r\n");
	sb.append("567\r\naaa");
	auto it = sb.begin();
	auto result = StringBuilder::extractLine(it);
	ASSERT_EQ(5, result.getLength());
	ASSERT_STREQ("01234", result.getValue());

	it.skipLine();
	result = StringBuilder::extractLine(it);
	ASSERT_EQ(3, result.getLength());
	ASSERT_STREQ("567", result.getValue());
}

TEST(StringBuilder, ExtractWithCRLineEnding) {
	StringBuilder sb("012");
	sb.append("34\r");
	sb.append("567\raaa");
	auto it = sb.begin();
	auto result = StringBuilder::extractLine(it);
	ASSERT_EQ(5, result.getLength());
	ASSERT_STREQ("01234", result.getValue());

	it.skipLine();
	result = StringBuilder::extractLine(it);
	ASSERT_EQ(3, result.getLength());
	ASSERT_STREQ("567", result.getValue());
}

TEST(StringBuilder, skip) {
	StringBuilder sb("012\n345");
	sb.append("6\n789");

	auto it = sb.begin();
	ASSERT_EQ('0', *it);
	ASSERT_EQ('0', it[0]);
	ASSERT_EQ('1', it[1]);

	++it;
	ASSERT_EQ('1', *it);
	ASSERT_EQ('1', it[0]);

	it.skipLine();
	ASSERT_EQ('3', *it);
	ASSERT_EQ('6', it[3]);

	it.skipLine();
	ASSERT_EQ('7', *it);

	it.skipAll();
	ASSERT_EQ('\0', *it);
}

TEST(StringBuilder, Clear) {

	StringBuilder sb("012\n345");
	sb.append("6\n789");
	sb.clear();

	sb.append("123\n456");
	const auto it = sb.begin() + 1;
	ASSERT_EQ('2', *it);
	ASSERT_EQ('4', it[3]);
}

TEST(StringBuilder, OperatorStar) {
	StringBuilder sb("012");
	sb.append("345");
	const auto it = sb.begin();
	ASSERT_EQ('0', *it);
	ASSERT_EQ('1', *(it + 1));
	ASSERT_EQ('2', *(it + 2));
	ASSERT_EQ('3', *(it + 3));
	ASSERT_EQ('4', *(it + 4));
	ASSERT_EQ('5', *(it + 5));
	ASSERT_EQ('\0', *(it + 6));
}

TEST(StringBuilder, OperatorIncrement) {
	StringBuilder sb("012");
	sb.append("34");
	auto it = sb.begin();
	ASSERT_EQ('0', *it); ++it;
	ASSERT_EQ('1', *it); ++it;
	ASSERT_EQ('2', *it); ++it;
	ASSERT_EQ('3', *it); ++it;
	ASSERT_EQ('4', *it); ++it;
	ASSERT_EQ('\0', *it); ++it;
	ASSERT_EQ('\0', *it); ++it;
	ASSERT_EQ('\0', *it); ++it;
}

TEST(StringBuilder, ClearOneBlock) {
	StringBuilder sb("0123456789");
	auto it = sb.begin();
	it = it + 2;

	ASSERT_EQ('2', *it);
	sb.clearBefore(it);

	auto result = sb.begin();
	ASSERT_EQ('2', *result);
	ASSERT_EQ('3', *(++result));
}

TEST(StringBuilder, ClearThreeBlocks) {
	StringBuilder sb("012");
	sb.append("34567");
	sb.append("89");
	auto it = sb.begin();
	it = it + 2;

	ASSERT_EQ('2', *it);
	ASSERT_EQ('3', *(it + 1));
	sb.clearBefore(it);

	it = sb.begin();
	ASSERT_EQ('2', *it);

	it = it + 7;
	ASSERT_EQ('9', *it);
	ASSERT_EQ('9', it[0]);

	sb.clearBefore(it);
	ASSERT_EQ('9', *sb.begin());
}


TEST(String, CreateSimple) {
	String s = "asdfg";
	ASSERT_EQ(5, s.getLength());
	ASSERT_STREQ("asdfg", s.getValue());
}

TEST(String, CreateCopy) {
	String s1 = "asdfg";
	String s2 = s1;
	String s3(s1);
	ASSERT_EQ(5, s1.getLength());
	ASSERT_EQ(5, s2.getLength());
	ASSERT_EQ(5, s3.getLength());

	ASSERT_STREQ("asdfg", s1.getValue());
	ASSERT_STREQ("asdfg", s2.getValue());
	ASSERT_STREQ("asdfg", s3.getValue());
}

TEST(String, Assignment) {
	String s1 = "test";
	String s2 = "asdfg";
	ASSERT_STRNE(s1.getValue(), s2.getValue());

	s2 = s1;
	ASSERT_EQ(4, s1.getLength());
	ASSERT_STREQ(s1.getValue(), s2.getValue());
	ASSERT_NE(s1.getValue(), s2.getValue());
}

TEST(String, Destructor) {
	//while(1)
	{
		String s1 = "0";
		{
			const String s2 = "11";
			s1 = s2;
		}
		ASSERT_STREQ("11", s1.getValue());
	}
}