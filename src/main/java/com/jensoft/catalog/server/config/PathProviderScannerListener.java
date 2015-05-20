package com.jensoft.catalog.server.config;

import org.jensoft.core.catalog.nature.Captcha;
import org.jensoft.core.catalog.nature.Catalog;
import org.jensoft.core.catalog.nature.JenSoftDashboard;
import org.jensoft.core.catalog.nature.JenSoftView;
import org.jensoft.core.catalog.nature.New;
import org.jensoft.core.catalog.nature.UIUnit;
import org.jensoft.core.catalog.nature.X2DView;
import org.jensoft.core.x2d.binding.X2DBinding;

public final class PathProviderScannerListener extends AnnotationScannerListener {

	public PathProviderScannerListener() {
		super(X2DBinding.class, X2DBinding.class, JenSoftDashboard.class, JenSoftView.class, UIUnit.class,Catalog.class,New.class,Captcha.class,X2DView.class);
	}

	public PathProviderScannerListener(ClassLoader classloader) {
		super(classloader, X2DBinding.class, JenSoftDashboard.class, JenSoftView.class,UIUnit.class,Catalog.class,New.class,Captcha.class,X2DView.class);
	}
}
