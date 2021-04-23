
#include "gtest/gtest.h"
#include <list>
#include "CLogReader.h"
#include "Util.h"

TEST(CLogReader, OneTimeParsingWithLast) {
	CLogReader reader;
	reader.setFilter("a*b");
	reader.addSourceBlock("aaa", 3);
	reader.addSourceBlock("b\n", 2);
	reader.addSourceBlock("aaa", 3);
	reader.addSourceBlock("bbb", 3);
	reader.addSourceBlock("c", 1);
	reader.addSourceBlock("\na111b\na222b\na333b\na11bc\n1a1bc\na444b", 37);

	std::list<String> result;
	const auto cb = [&result](const String& item) {
		result.push_back(item);
	};
	reader.parse(true, cb);
	ASSERT_EQ(5, result.size());
}

TEST(CLogReader, OneTimeParsingWithoutLast) {
	CLogReader reader;
	reader.setFilter("a*b");
	reader.addSourceBlock("aaa", 3);
	reader.addSourceBlock("b\n", 2);
	reader.addSourceBlock("aaa", 3);
	reader.addSourceBlock("bbb", 3);
	reader.addSourceBlock("c", 1);
	reader.addSourceBlock("\na111b\na222b\na333b\na11bc\n1a1bc\na444b", 37);

	std::list<String> result;
	const auto cb = [&result](const String& item) {
		result.push_back(item);
	};
	reader.parse(false, cb);
	ASSERT_EQ(4, result.size());
}

void add(CLogReader& reader, const char* str) {
	reader.addSourceBlock(str, util::length(str));
}
void add(CLogReader* reader, const char* str) {
	reader->addSourceBlock(str, util::length(str));
}

TEST(CLogReader, OneBlockOneLineParsing) {
	CLogReader reader;
	reader.setFilter("a*b");
	std::vector<String> result;
	const auto cb = [&result](const String& item) {
		result.push_back(item);
	};

	add(reader, "aaa\n");
	reader.parse(false, cb);

	add(reader, "aab\r\n");
	reader.parse(false, cb);

	add(reader, "aaa\n");
	reader.parse(false, cb);

	add(reader, "aaab\n");
	reader.parse(false, cb);

	add(reader, "aaa");
	reader.parse(true, cb);
	ASSERT_EQ(2, result.size());
	ASSERT_STREQ("aab", result[0].getValue());
	ASSERT_STREQ("aaab", result[1].getValue());
}


TEST(CLogReader, LoadTestSingleLinePerBlock) {
	std::vector<String> items;
	const auto cb = [&items](const String& item) {
		items.push_back(item);
	};

	auto* reader = new CLogReader();
	reader->setFilter("*");

	add(reader, "11\n2");
	reader->parse(false, cb);
	ASSERT_EQ(1, items.size());
	ASSERT_STREQ("11", items[0].getValue());

	add(reader, "2\n3");
	reader->parse(false, cb);
	ASSERT_EQ(2, items.size());
	ASSERT_STREQ("11", items[0].getValue());
	ASSERT_STREQ("22", items[1].getValue());


	reader->addSourceBlock("3", 1);
	reader->parse(false, cb);
	ASSERT_EQ(2, items.size());
	ASSERT_STREQ("11", items[0].getValue());
	ASSERT_STREQ("22", items[1].getValue());

	reader->parse(true, cb);
	ASSERT_EQ(3, items.size());
	ASSERT_STREQ("11", items[0].getValue());
	ASSERT_STREQ("22", items[1].getValue());
	ASSERT_STREQ("33", items[2].getValue());

	delete reader;
}


TEST(CLogReader, LoadTest2TwoLinesPerBlock) {
	std::vector<String> items;
	const auto cb = [&items](const String& item) {
		items.push_back(item);
	};

	auto* reader = new CLogReader();
	reader->setFilter("*");

	add(reader, "11\n22\n3");
	reader->parse(false, cb);
	ASSERT_EQ(2, items.size());
	ASSERT_STREQ("11", items[0].getValue());
	ASSERT_STREQ("22", items[1].getValue());

	add(reader, "3\n44\n55");
	reader->parse(false, cb);
	ASSERT_EQ(4, items.size());
	ASSERT_STREQ("33", items[2].getValue());
	ASSERT_STREQ("44", items[3].getValue());


	add(reader, "\n66\n77");
	reader->parse(false, cb);
	ASSERT_EQ(6, items.size());
	ASSERT_STREQ("55", items[4].getValue());
	ASSERT_STREQ("66", items[5].getValue());

	reader->parse(true, cb);
	ASSERT_EQ(7, items.size());
	ASSERT_STREQ("77", items[6].getValue());

	delete reader;
}
