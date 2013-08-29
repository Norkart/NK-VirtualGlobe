/*
 * Created on Jun 24, 2004
 *
 */
package com.sun.syndication.unittest;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import java.util.List;

/**
 * @author pat
 *
 */
public class TestSyndFeedRSS20 extends TestSyndFeedRSS094 {

	public TestSyndFeedRSS20() {
		super("rss_2.0");
	}

    protected TestSyndFeedRSS20(String type) {
        super(type);
    }

    protected TestSyndFeedRSS20(String feedType,String feedFileName) {
        super(feedType,feedFileName);
    }
    
    protected void _testItem(int i) throws Exception {
        super._testItem(i);
        List items = getCachedSyndFeed().getEntries();
        SyndEntry entry = (SyndEntry) items.get(i);
        assertProperty(((SyndContent)entry.getContents().get(0)).getValue(), "channel.item["+i+"].content");
    }

}
