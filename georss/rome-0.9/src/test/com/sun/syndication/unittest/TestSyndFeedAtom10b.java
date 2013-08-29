package com.sun.syndication.unittest;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestSyndFeedAtom10b extends FeedTest {
	public TestSyndFeedAtom10b() {
		super("atom_1.0_b.xml");
	}

	public void testXmlBaseConformance() throws Exception {
		List errors = new ArrayList();
		SyndFeed feed = getSyndFeed();
		List entries = feed.getEntries();
		for (int index = 0; index < entries.size(); index++) {
			SyndEntry entry = (SyndEntry) entries.get(index);
            assertEquals(
                "Incorrect URI: " + entry.getLink() + " in entry [" + entry.getTitle() + "]",
                "http://example.org/tests/base/result.html", entry.getLink());
		}
	}
}
