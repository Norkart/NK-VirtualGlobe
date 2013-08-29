package com.sun.syndication.unittest;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestSyndFeedAtom10Ruby extends FeedTest {
	public TestSyndFeedAtom10Ruby() {
		super("atom_1.0_ruby.xml");
	}

	public void testFeedURI() throws Exception {
        SyndFeed feed = getSyndFeed();
		assertEquals("http://www.example.com/blog", feed.getUri());
	}
	public void testEntry1URI() throws Exception {
        SyndFeed feed = getSyndFeed();
        SyndEntry entry = (SyndEntry)feed.getEntries().get(0);
        assertEquals("http://www.example.com/blog/bloggy-blog", entry.getLink());
	}
	public void testEntry2URI() throws Exception {
        SyndFeed feed = getSyndFeed();
        SyndEntry entry = (SyndEntry)feed.getEntries().get(1);
        assertEquals("http://www.example.com/frog/froggy-frog", entry.getLink());
	}
}
