package org.xj3d.browser.app;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import org.xj3d.ui.swt.view.BrowserViewConstants;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		
		layout.addStandaloneView(BrowserViewConstants.BROWSER_VIEW_ID, false, IPageLayout.LEFT, 1.0f, editorArea);
	}

}
