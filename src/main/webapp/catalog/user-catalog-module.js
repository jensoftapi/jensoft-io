
getConnector = function() {
			var xhr = null;		
			if (window.XMLHttpRequest || window.ActiveXObject) {
				if (window.ActiveXObject) {
					try {
						xhr = new ActiveXObject("Msxml2.XMLHTTP");
					} catch(e) {
						xhr = new ActiveXObject("Microsoft.XMLHTTP");
					}
				} else {
					xhr = new XMLHttpRequest(); 
				}
			} else {
				return false;
			}
			return xhr;
};

		
prettyPrint = function(){
	$(document).ready(function() {
		  $('pre code').each(function(i, block) {
		    hljs.highlightBlock(block);
		  });
	});
};


var JenSoft ={};
JenSoft.CatalogServer ='';
function getCatalogInstance(group,artifact){
	return window["jensoft_cat_"+group.replace('.','_')+'_'+artifact.replace('-','_')];
}

JenSoft.Catalog = function(config){
	config = config || {};
	
	//alert('create catalog');
	//CATALOG
	//conf = all(all views or all units) or conf = unit(all views of a unit)
	//feature view   : allows to open chart (view or dashboard)
	//feature source : allows to open source of component (view,dashboard,see resources)
	
	var catalogContainer = config.container;
	var catalogGroup = config.group;
	var catalogArtifact = config.artifact;
	
	
	var catalogAPIServicePattern = '/api';


	//current catalog
	var pageCatalog = 0;
	var pagesCatalogFrameStart = 0;
	var pagesCatalogFrameEnd = 7;
	var itemCatalogPerPage = 4;
	var catalogSize = undefined;
	var pageCatalogCount = undefined;
	
	var catalogSearchPattern = config.topic ;

	var conf = 'all';
	var mode = 'views' ;

	var catalog = undefined;

	//current sub catalog for the open unit
	var pageCatalogUnit = 0;
	var pagesCatalogUnitFrameStart = 0;
	var pagesCatalogUnitFrameEnd = 7;
	var itemCatalogUnitPerPage = 4;
	var catalogUnitSize = undefined;
	var pageCatalogUnitCount = undefined;
	var catalogUnitSearchPattern = undefined;
	var units = undefined;


	//var maximize = false;

	var views = undefined;

	var selectedUnit = undefined;

	var contentBeforeAccessApplet = undefined;
	var contentBeforeAccessView = undefined;
	var contentBeforeAccessSource = undefined;
	var contentBeforeAccessUnit = undefined;



	//var metaDef = [];

	CatalogDef = {
			id:'',
			name: '',
			group:'',
			artifact:'',
			version:'',
			core:'',
			description:'',
			countCharts:'',
			countUnits:'',
			visible:'',
			server:'',
				
			dump : function(){
				console.info('catalog definition');
				console.info('catalog name :'+this.name);
				console.info('catalog group :'+this.group);
				console.info('catalog artifact :'+this.artifact);
				console.info('catalog version :'+this.version);
				console.info('catalog count charts :'+this.countCharts);
				console.info('catalog count units :'+this.countUnits);
				console.info('catalog core :'+this.core);
			}
	};



	/**
	 * get catalog base : /server/URI
	 */
	function getCatalogBase(){	
		var catalogAPIContext = JenSoft.CatalogServer+getCatalogURI();
		//alert("Catalog base : "+catalogAPIContext);
		return catalogAPIContext;
	}

	/**
	 * get catalog URI which looks like : /catalog/group/artifact
	 */
	function getCatalogURI(){
		//var catalogBaseContext = '/catalog/'+catalogGroup.replace('.','/')+'/'+catalogArtifact;
		//var catalogBaseContext = catalogGroup.replace('.','/')+'/'+catalogArtifact;
		//return catalogBaseContext;
		return "/jensoft-io";
	}

	/**
	 * get catalog api context : /server/URI/api
	 */
	function getCatalogAPIContext(){
		var catalogAPIContext = getCatalogBase()+catalogAPIServicePattern;
		return catalogAPIContext;
	}

	/**
	 * get catalog web start base : /server/URI/webstart
	 */
	function getWebStartBase(){
		var catalogWebstartBase = getCatalogBase()+'/webstart';
		return catalogWebstartBase;
	}

	/**
	 * get catalog web start base : /server/URI/resources
	 */
	function getResourceBase(){
		var catalogResourceBase = getCatalogBase()+'/resources';
		return catalogResourceBase;
	}
	
	/**
	 * open catalog in the given div holder
	 */
	function openCatalog(){	
		$(".popover").remove();
		initFrame();
		$('#'+catalogContainer).load('catalog/user-catalog-template.html',function() {
			//re - GET the real definition from server itself 
			var xhr = getConnector();		
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4 && xhr.status == 200) {
					
					var catalogDef = xhr.responseXML;
					CatalogDef.name = $(catalogDef).find('name').text();
					CatalogDef.group = $(catalogDef).find('group').text();
					CatalogDef.artifact = $(catalogDef).find('artifact').text();
					CatalogDef.version = $(catalogDef).find('version').text();
					CatalogDef.core = $(catalogDef).find('core').text();
					CatalogDef.description = $(catalogDef).find('description').text();
					CatalogDef.countCharts = $(catalogDef).find('count-charts').text();
					CatalogDef.countUnits = $(catalogDef).find('count-units').text();
					
					displayTitle();
					
//					if(mode == 'views'){
//						installCatalogViews();
//					}else if(mode == 'units'){
//						installCatalogUnits();
//					}
					installCatalogViews();
				}
			};
			//alert("get : "+getCatalogAPIContext()+'/def');
			xhr.open("GET", getCatalogAPIContext()+'/def', true);	
			xhr.send();	
		});
	}
	
	function displayTitle(){
		$("#catalogTitle").html('Catalog  '+CatalogDef.name);
		$("#catalogSubTitle").html(CatalogDef.name+' browser');
		$("#catalogWelcome").html(
				'You are in the catalog of group <strong>'+
				CatalogDef.group+'</strong> with artifact <strong>'+
				CatalogDef.artifact+'</strong> of version <strong>'+
				CatalogDef.version+'</strong> and jensoft api core version '+
				CatalogDef.core+'.&nbsp;'+
				'Catalog samples contains  <strong>'+CatalogDef.countCharts+' views</strong> and <strong>'+CatalogDef.countUnits+' Units</strong>.&nbsp;'+
				'Read version <a href="#" onclick="getCatalogInstance(\'org.jensoft\',\'jensoft-samples\').openReleaseNote();" style="text-align: right;" >release note</a>&nbsp;'+
				'and <a href="#" onclick="getCatalogInstance(\'org.jensoft\',\'jensoft-samples\').openLicenseNote();" style="text-align: right;" >license</a>'
		);
	}

	function displayCatalogPopup(){
		$(".popover").remove();
		
		var catImageURL = getCatalogAPIContext()+'/icon/48';
		imageHtml = '<img class="img-responsive" src="'+catImageURL+'">';
		$.get("catalog/catalog-popup-template.html", function(popupContent) {
			
			var m = getMeta(id);
			
			$("#catalog_"+id).popover({trigger: 'manual',title:'hello',html:true,content : popupContent});
			
			$("#catalog_"+id).popover('show');
			$("#catalogPopupImage").html(imageHtml);
			$("#catalogPopupTitle").html(m.name);
			$("#catalogPopupDesccription").html(m.description);
			
			$("#catalogPopupCountCharts").html(m.countCharts+" Charts");
			$("#catalogPopupCountUnits").html(m.countUnits+" Units");

			$(".popover").attr('style',$(".popover").attr('style')+'padding : 0px;border-radius: 0px 0px 0px 0px; border : 1px solid rgb(190,190,190);');
			$(".popover-title").remove();
			$(".popover-content").attr('style','padding: 0px 0px;');
			
		});
	}


	/**
	 * initialize frame
	 */
	function initFrame(){
		pageCatalog = 0;
		pagesCatalogFrameStart = 0;
		pagesCatalogFrameEnd = 7;
		itemCatalogPerPage = 4;
		
		pageCatalogUnit = 0;
		pagesCatalogUnitFrameStart = 0;
		pagesCatalogUnitFrameEnd = 7;
		itemCatalogUnitPerPage = 4;
	}


	/**
	 * switch catalog in given mode m : views or units
	 */
	function switchCatalog(m){
		$(".popover").remove();
		mode=m;
		pageCatalog=0;
		pagesCatalogFrameStart = 0;
		pagesCatalogFrameEnd = 7;
		if(mode == 'views'){
			installCatalogViews();
		}else if(mode == 'units'){
			installCatalogUnits();
		}
		checkButtonSwitch();
	}

	/**
	 * control switch button
	 */
	function checkButtonSwitch(){
		$("#catalog-switch-views").attr('class','btn');
		$("#catalog-switch-units").attr('class','btn');
		if(mode == 'views'){
			$("#catalog-switch-views").attr('class','btn  btn-primary');
		}else if(mode == 'units'){
			$("#catalog-switch-units").attr('class','btn btn-primary');
		}
	}


	/**
	 * search within catalog with current pattern
	 */
	function searchCatalog(){
			catalogSearchPattern = $("#inputAdminSearchCatalog").val();		
			var xhr = getConnector();		
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4 && xhr.status == 200) {
					if(xhr.responseText !== undefined){
						catalogSize = parseInt(xhr.responseText);
						intPart = parseInt(catalogSize/itemCatalogPerPage);
						if((catalogSize - intPart*itemCatalogPerPage) > 0){
							pageCatalogCount = intPart +1 ;
						}else{
							pageCatalogCount = intPart;
						}
						
						if(pageCatalogCount > 8){ //fixed size to 8
							pagesCatalogFrameStart = 0;
							pagesCatalogFrameEnd = 7;
						}else{
							pagesCatalogFrameStart = 0;
							pagesCatalogFrameEnd = pageCatalogCount-1;
						}
						
						
						if(mode == 'views'){
							selectAdminCatalogViewsPage(0);
						} else if(mode == 'units'){
							selectAdminCatalogUnitsPage(0);
						}
						
					}
				}
			};
			xhr.open("GET", getCatalogAPIContext()+'/'+mode+'/size/search/'+encodeURIComponent($("#inputAdminSearchCatalog").val()), true);	
			xhr.send();	
	}

	
	/**
	 * search within catalog with current search unit pattern
	 */
	function searchCatalogUnit(){
		catalogUnitSearchPattern = $("#inputAdminSearchCatalogUnit").val();		
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseText !== undefined){
					catalogUnitSize = parseInt(xhr.responseText);
					countCatalogUnitPage();
					
					if(pageCatalogUnitCount > 8){ //fixed size to 8
						pagesCatalogUnitFrameStart = 0;
						pagesCatalogUnitFrameEnd = 7;
					}else{
						pagesCatalogUnitFrameStart = 0;
						pagesCatalogUnitFrameEnd = pageCatalogUnitCount-1;
					}
					installAdminCatalogUnitViews($(selectedUnit).find('name').text());
				}
			}
		};
		var service = getCatalogAPIContext()+'/unit/'+$(selectedUnit).find('name').text()+'/size/search/'+encodeURIComponent($("#inputAdminSearchCatalogUnit").val());
		xhr.open("GET", service, true);
		xhr.send();	
	}

	/**
	 * install catalog units
	 */
	function installCatalogUnits(){
		
		initializeCatalogContext();
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseText !== undefined){
					catalogSize = parseInt(xhr.responseText);
				}
			}
		};
		xhr.open("GET", getCatalogAPIContext()+'/units/size/search/'+encodeURIComponent($("#inputAdminSearchCatalog").val()), true);	
		xhr.send();
		selectAdminCatalogUnitsPage(pageCatalog);
		checkButtonSwitch();
	}


	/**
	 * initialize catalog context
	 */
	function initializeCatalogContext(){
		if(catalogSearchPattern !== undefined){
			$("#inputAdminSearchCatalog").val(catalogSearchPattern);	
		}
		if(catalogUnitSearchPattern !== undefined){
			$("#inputAdminSearchCatalogUnit").val(catalogUnitSearchPattern);	
		}
	}


	function installCatalogViews(){
		initializeCatalogContext();
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseText !== undefined){
					catalogSize = parseInt(xhr.responseText);
					selectAdminCatalogViewsPage(pageCatalog);
					checkButtonSwitch();
				}
			}
		};
		var viewsLookUpURI = getCatalogAPIContext()+'/views/size/search/'+encodeURIComponent($("#inputAdminSearchCatalog").val());
		xhr.open("GET",viewsLookUpURI , true);
		xhr.send();
	}

	function updateCatalogPageLabelHeader(){
		if(pageCatalogCount > 0){
			$("#catalog-current-page").html('<small> <span class="boldblue1"> Page '+parseInt(pageCatalog+1)+' on '+(pageCatalogCount)+' pages</span></small>');
		}else{
			$("#catalog-current-page").html('<small> <span class="boldblue1">No Pages</span></small>');
		}
	}

	function updateCatalogUnitPageLabelHeader(){
		if(pageCatalogUnitCount > 0){
			$("#catalog-unit-current-page").html('Page '+parseInt(pageCatalogUnit+1)+' on '+(pageCatalogUnitCount)+' pages');
		}else{
			$("#catalog-unit-current-page").html('No Pages');
		}
	}

	function updateCatalogLookupMessageLabelHeader(){
		if($("#inputAdminSearchCatalog").val() !== 'undefined' && $("#inputAdminSearchCatalog").val() != null && $("#inputAdminSearchCatalog").val() !== ''){
			$("#catalog-search-message").html('<span class="boldblue1"><small>'+catalogSize+'&nbsp; view(s) lookup for \''+$("#inputAdminSearchCatalog").val()+'\'</small></span>');
		}else{
			$("#catalog-search-message").html('<span class="boldblue1"><small>All Catalog</small></span>');
		}
	}

	function updateCatalogUnitLookupMessageLabelHeader(){
		if($("#inputAdminSearchCatalogUnit").val() !== 'undefined' && $("#inputAdminSearchCatalogUnit").val() != null && $("#inputAdminSearchCatalogUnit").val() !== ''){
			$("#catalog-unit-search-message").html('<span class="boldblue1"><small>'+catalogUnitSize+'&nbsp; view(s) lookup for \''+$("#inputAdminSearchCatalogUnit").val()+'\'</small></span>');
		}else{
			$("#catalog-unit-search-message").html('<span class="boldblue1"><small>All Catalog</small></span>');
		}
	}


	function switchCatalogFrame(step){
		 pagesCatalogFrameStart = parseInt(pagesCatalogFrameStart+parseInt(step));
		 pagesCatalogFrameEnd = parseInt(pagesCatalogFrameEnd+parseInt(step));
	}

	function switchCatalogUnitFrame(step){
		 pagesCatalogUnitFrameStart = parseInt(pagesCatalogUnitFrameStart+parseInt(step));
		 pagesCatalogUnitFrameEnd = parseInt(pagesCatalogUnitFrameEnd+parseInt(step));
	}


	function solveCatalogPager(pageIndex){
		var xhr = getConnector();
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseText !== undefined){
					catalogSize = parseInt(xhr.responseText);
					updateCatalogLookupMessageLabelHeader();
					pageCatalog=pageIndex;
					countCatalogPage();
					updateCatalogPageLabelHeader();
					if(pageIndex == pagesCatalogFrameStart && pageIndex != 0){
						switchCatalogFrame(-1);
					}
					else if(pageIndex == pagesCatalogFrameEnd && pageIndex != pageCatalogCount-1){
						switchCatalogFrame(1);
					}
					buildCatalogHTMLPager();
					styleCatalogActivePage();
				}
			}
		};
		xhr.open("GET", getCatalogAPIContext()+'/'+mode+'/size/search/'+encodeURIComponent($("#inputAdminSearchCatalog").val()), true);	
		xhr.send();	
	}


	function countCatalogPage(){
		intPart = parseInt(catalogSize/itemCatalogPerPage);
		if((catalogSize - intPart*itemCatalogPerPage) > 0){
			pageCatalogCount = intPart +1 ;
		}else{
			pageCatalogCount = intPart;
		}
		
		
		if(pageCatalogCount > 8){ //fixed size to 8
//			alert("rezie frame");
//			pagesCatalogFrameStart = 0;
//			pagesCatalogFrameEnd = 7;
			//frame keeps going
		}else{
			pagesCatalogFrameStart = 0;
			pagesCatalogFrameEnd = pageCatalogCount-1;
		}
//		alert("page catalog count: "+pageCatalogCount);
//		alert("page catalog frame start: "+pagesCatalogFrameStart);
//		alert("page catalog frame end: "+pagesCatalogFrameEnd);
	}

	function countCatalogUnitPage(){
		intPart = parseInt(catalogUnitSize/itemCatalogUnitPerPage);
		if((catalogUnitSize - intPart*itemCatalogUnitPerPage) > 0){
			pageCatalogUnitCount = intPart +1 ;
		}else{
			pageCatalogUnitCount = intPart;
		}
		
		if(pageCatalogUnitCount > 8){ //fixed size to 8
			//frame keeps going
		//	pagesCatalogUnitFrameStart = 0;
		//	pagesCatalogUnitFrameEnd = 7;
			
		}else{
			pagesCatalogUnitFrameStart = 0;
			pagesCatalogUnitFrameEnd = pageCatalogUnitCount-1;
		}
	}
		

	function buildCatalogHTMLPager(){
		$("#catalog-pagination").html('');
		if(pageCatalogCount == 0){
			 return;
		}
		if(pageCatalogCount == 1){
			 html = '<nav>';
			 html = html + '<ul class="pagination" style="margin : 0;">';
			 html = html + '<li id="catalog-page0" ><a href="#">'+'1'+'</a></li>';
			 html = html + '</ul></nav>';
			 $("#catalog-pagination").html(html);
			 return;
		}
		
		 firstLabel = pagesCatalogFrameStart+1;
		 if(pagesCatalogFrameStart != 0){
			 firstLabel="Prev"; 
		 }
		 lastLabel = pagesCatalogFrameEnd+1;
		 if(pagesCatalogFrameEnd != pageCatalogCount-1){
			 lastLabel="Next"; 
		 }
		
		 html = '<nav>';
		 html = html + '<ul class="pagination" style="margin : 0;">';
		 html = html + '<li id="catalog-page'+pagesCatalogFrameStart+'"><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').selectCatalogPage('+pagesCatalogFrameStart+');return false;">'+firstLabel+'</a></li>';
		 for(var i = pagesCatalogFrameStart+1; i < pagesCatalogFrameEnd; ++i){
			 html = html + '<li id="catalog-page'+i+'"><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').selectCatalogPage('+i+');return false;">'+parseInt(i+1)+'</a></li>';
		 }
		 html = html + '<li id="catalog-page'+pagesCatalogFrameEnd+'"><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').selectCatalogPage('+pagesCatalogFrameEnd+');return false;">'+lastLabel+'</a></li>';
		 html = html + '</ul></nav>';
		 $("#catalog-pagination").html(html);
	}

	function buildCatalogUnitHTMLPager(unit){
		$("#catalog-unit-pagination").html('');
		if(pageCatalogUnitCount == 1){
			 html = '<nav>';
			 html = html + '<ul class="pagination" style="margin : 0;">';
			 html = html + '<li id="catalog-unit-page0" ><a href="#">'+'1'+'</a></li>';
			 html = html + '</ul></nav>';
			 $("#catalog-unit-pagination").html(html);
			 return;
		}
		 firstLabel = pagesCatalogUnitFrameStart+1;
		 if(pagesCatalogUnitFrameStart != 0){
			 firstLabel="Prev"; 
		 }
		 lastLabel = pagesCatalogUnitFrameEnd+1;
		 if(pagesCatalogUnitFrameEnd != pageCatalogUnitCount-1){
			 lastLabel="Next"; 
		 }
		
		 html = '<nav>';
		 html = html + '<ul class="pagination" style="margin : 0;">';
		 html = html + '<li id="catalog-unit-page'+pagesCatalogUnitFrameStart+'"><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').selectPageUnitViews(\''+unit+'\','+pagesCatalogUnitFrameStart+');return false;">'+firstLabel+'</a></li>';
		 for(var i = pagesCatalogUnitFrameStart+1; i < pagesCatalogUnitFrameEnd; ++i){
			 html = html + '<li id="catalog-unit-page'+i+'"><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').selectPageUnitViews(\''+unit+'\','+i+');return false;">'+parseInt(i+1)+'</a></li>';
		 }
		 html = html + '<li id="catalog-unit-page'+pagesCatalogUnitFrameEnd+'"><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').selectPageUnitViews(\''+unit+'\','+pagesCatalogUnitFrameEnd+');return false;">'+lastLabel+'</a></li>';
		 html = html + '</ul></nav>';
		 $("#catalog-unit-pagination").html(html);
	}



	function styleCatalogActivePage(){
		for(var k = pagesCatalogFrameStart; k <= pagesCatalogFrameEnd; ++k){
			$("#catalog-page"+k).attr('class','');
		}
		
		$("#catalog-page"+pageCatalog).attr('class','active');
	}

	function styleCatalogUnitActivePage(){
		for(var k = pagesCatalogUnitFrameStart; k <= pagesCatalogUnitFrameEnd; ++k){
			$("#catalog-unit-page"+k).attr('class','');
		}	
		$("#catalog-unit-page"+pageCatalogUnit).attr('class','active');
	}

	function selectCatalogPage(pageIndex){
		if(mode == 'views'){
			selectAdminCatalogViewsPage(pageIndex);
		}
		else if(mode == 'units'){
			selectAdminCatalogUnitsPage(pageIndex);
		}
	}

	function getViewFromCache(viewClass,viewPackage){
		if(views.length > 0){		
			 for(var i = 0; i < views.length; ++i){
				 var view = views[i];
				 var className =$(view).find('class');
				 var packageName =$(view).find('package');
				 if(viewClass == className.text() && viewPackage == packageName.text()){
					 return view;
				 }
			 }
		}
		return false;
	}

	function selectAdminCatalogViewsPage(pageIndex){
		solveCatalogPager(pageIndex);
		
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseXML !== undefined){
					views = $(xhr.responseXML).find('view');
					var html ='';
					if(views.length > 0){
						var indexCount =  pageIndex*itemCatalogPerPage;

						for(var i = 0; i < views.length; ++i){
							 var view = views[i];
							 var className =$(view).find('class');
							 var packageName =$(view).find('package');
							 var unitName =$(view).find('unit');
							 var description =$(view).find('description');
							 var type =$(view).find('type');
							 
							 mountImageInGalleryHeader(i,packageName,className);
							
							 html = html+'<tr>';
							 html = html + '<td><small>'+indexCount+'</small></td>';
							 html = html + '<td><small><a href="#" rel="popover" data-trigger="hover" onmouseover="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').displayImage('+i+',\''+className.text()+'\',\''+packageName.text()+'\');return false;" id="tr_'+i+'" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openCatalogView(\''+className.text()+'\',\''+packageName.text()+'\',\'all\');return false;">'+className.text()+'</a></small><br><span class="verysmall">'+packageName.text()+'</span></td>';
							 html = html + '<td><small><span class="verysmall2 colorblue1">'+description.text()+'</span></small></td>';
							 html = html + '<td><small><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openCatalogUnit(\''+unitName.text()+'\');return false;">'+unitName.text()+'<a></small></td>';
							 html = html + '</tr>';
							 indexCount = indexCount+1;
						 }
						 if(views.length < itemCatalogPerPage){
							 for(var i = views.length; i < itemCatalogPerPage; ++i){
								 html = html+'<tr><td>--</td><td>--<br>--</td><td>--</td><td>--</td></tr>';
								 clearImage(i+1);
							 }
						 }
					}else{
						html = html+'<tr><td><small><span class="boldorange">Result(0)</span></small></td><td><small>search word matches any view name or package</small></td><td></td><td></td></tr>';
					}
					$("#catalog-page-header").html('<tr><th></th><th></th><th></th><th></th><tr><tr><th><small><small></th><th><small><span class="boldblue1">Chart</span><small></th><th><small><span class="boldblue1">Description</span><small></th><th><small><span class="boldblue1">Unit</span><small></th></tr>');
					$("#catalog-page-body").html(html);
					updateCatalogPageLabelHeader();
					highligthSearch();
				}
			}
		};
		xhr.open("GET", getCatalogAPIContext()+'/views/page/'+pageIndex+'/viewsperpage/'+itemCatalogPerPage+'/search/'+encodeURIComponent($("#inputAdminSearchCatalog").val()), true);	
		xhr.send();	
	}


	function clearImageInGalleryHeader(){
		 $("#img1").html("");
		 $("#img2").html("");
		 $("#img3").html("");
		 $("#img4").html("");
		 
//		 if(maximize){
//			 $("#img5").html("");
//			 $("#img6").html("");
//		 }
	}

	function clearImage(rank){
		 $("#img"+rank).html("");
	}


	/**
	 * mount view in header gallery
	 */
	function mountImageInGalleryHeader(rank,packageName,className){
		v = getViewFromCache(className.text(), packageName.text());
		//console.log("mountImageInGalleryHeader for view class cache: "+packageName.text()+"."+className.text());
		//console.log("mountImageInGalleryHeader for type : "+$(v).find("type").text());
		var catalogAPI = getCatalogAPIContext();
		//console.log("catalogAPI : "+catalogAPI);
		if($(v).find("type").text() == 'view'){
			 lookupView = packageName.text()+'.'+className.text();
			 imageURL = catalogAPI+'/view/'+encodeURIComponent(lookupView)+'/image';
			 imageHtml = '<img class="img-responsive" src="'+imageURL+'">';
			 if(rank==0){
				 $("#img1").html(imageHtml);
			 }
			 if(rank==1){
				 $("#img2").html(imageHtml);
			 }
			 if(rank==2){
				 $("#img3").html(imageHtml);
			 }
			 if(rank==3){
				 $("#img4").html(imageHtml);
			 }
//			 if(maximize){
//				 if(rank==4){
//					 $("#img5").html(imageHtml);
//				 }
//				 if(rank==5){
//					 $("#img6").html(imageHtml);
//				 }
//			 }
		}else{
			var xhr = getConnector();	
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4 && xhr.status == 200) {
					if(xhr.responseXML !== undefined){
						dashBoardviews = $(xhr.responseXML).find('view');
						//alert("get dashboard: "+dashBoardviews.length);
						var html ='';
						if(dashBoardviews.length > 0){
							
							html = '';
							if(dashBoardviews.length == 2){
								console.log("dashboard has 2 views");
								var img1 = '<img class="img-responsive" width="160"  src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[0]).find('package').text()+'.'+$(dashBoardviews[0]).find('class').text())+'/image/360/160'+'">';
								var img2 = '<img class="img-responsive" width="160"  src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[1]).find('package').text()+'.'+$(dashBoardviews[1]).find('class').text())+'/image/360/160'+'">';
								html = '<table><tr><td>'+img1+'</td></tr><tr><td>'+img2+'</td></tr></table>';
							}
							if(dashBoardviews.length == 4){
								console.log("dashboard has 4 views");
								var img1 = '<img class="img-responsive" width="80" height="60" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[0]).find('package').text()+'.'+$(dashBoardviews[0]).find('class').text())+'/image'+'">';
								var img2 = '<img class="img-responsive" width="80" height="60" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[1]).find('package').text()+'.'+$(dashBoardviews[1]).find('class').text())+'/image'+'">';
								var img3 = '<img class="img-responsive" width="80" height="60" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[2]).find('package').text()+'.'+$(dashBoardviews[2]).find('class').text())+'/image'+'">';
								var img4 = '<img class="img-responsive" width="80" height="60" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[3]).find('package').text()+'.'+$(dashBoardviews[3]).find('class').text())+'/image'+'">';
								html = '<table><tr><td>'+img1+'</td><td>'+img2+'</td></tr><tr><td>'+img3+'</td><td>'+img4+'</td></tr></table>';
							}

							 if(rank==0){
								 $("#img1").html(html);
							 }
							 if(rank==1){
								 $("#img2").html(html);
							 }
							 if(rank==2){
								 $("#img3").html(html);
							 }
							 if(rank==3){
								 $("#img4").html(html);
							 }
//							 if(maximize){
//								 if(rank==4){
//									 $("#img5").html(html);
//								 }
//								 if(rank==5){
//									 $("#img6").html(html);
//								 }
//							 }
						}else{
						}
					}
				}
			};
			
			var dashBoardPI = getCatalogAPIContext()+'/dashboard/'+packageName.text()+"."+className.text();
			xhr.open("GET", dashBoardPI, true);	
			xhr.send();
			
		}

	}

	//var popoverId;
	
	/**
	 * display view image on link roll over
	 */
	function displayImage(row,viewClass,viewPackage){
		$(".popover").remove();
		v = getViewFromCache(viewClass, viewPackage);
		if($(v).find("type").text() == "view"){
			lookupView = viewPackage+'.'+viewClass;
			imageURL = getCatalogAPIContext()+'/view/'+encodeURIComponent(lookupView)+'/image';
			imageHtml = '<img class="img-responsive" src="'+imageURL+'">';
			$("#tr_"+row).popover({ title: 'View '+viewClass, content: imageHtml, html:true });
			$("#tr_"+row).popover('show');		
		}else if($(v).find("type").text() == "dashboard"){
			displayDashboard(row,viewClass,viewPackage);
		}else{
			console.warn("displayImage WARNING with not type : "+$(v).find("type").text());
		}
	}

	/**
	 * display dashboard on link roll over
	 */
	function displayDashboard(row,viewClass,viewPackage){
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseXML !== undefined){
					var catalogAPI = getCatalogAPIContext();
					dashBoardviews = $(xhr.responseXML).find('view');
					var html ='';
					if(dashBoardviews.length > 0){
						html = '';
						if(dashBoardviews.length == 2){
							var img1 = '<img class="img-responsive" width="180" height="140" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[0]).find('package').text()+'.'+$(dashBoardviews[0]).find('class').text())+'/image'+'">';
							var img2 = '<img class="img-responsive" width="180" height="140" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[1]).find('package').text()+'.'+$(dashBoardviews[1]).find('class').text())+'/image'+'">';
							html = '<table><tr><td>'+img1+'</td><td>'+img2+'</td></tr></table>';
						}
						if(dashBoardviews.length == 4){
							var img1 = '<img class="img-responsive" width="180" height="140" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[0]).find('package').text()+'.'+$(dashBoardviews[0]).find('class').text())+'/image'+'">';
							var img2 = '<img class="img-responsive" width="180" height="140" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[1]).find('package').text()+'.'+$(dashBoardviews[1]).find('class').text())+'/image'+'">';
							var img3 = '<img class="img-responsive" width="180" height="140" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[2]).find('package').text()+'.'+$(dashBoardviews[2]).find('class').text())+'/image'+'">';
							var img4 = '<img class="img-responsive" width="180" height="140" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[3]).find('package').text()+'.'+$(dashBoardviews[3]).find('class').text())+'/image'+'">';
							html = '<table><tr><td>'+img1+'</td><td>'+img2+'</td></tr><tr><td>'+img3+'</td><td>'+img4+'</td></tr></table>';
						}
						 $("#tr_"+row).popover({ title: 'Dashboard '+viewClass, content: html, html:true });
						 $("#tr_"+row).popover('show');		
					}else{
					}
				}
			}
		};
		xhr.open("GET", getCatalogAPIContext()+'/dashboard/'+viewPackage+"."+viewClass, true);	
		xhr.send();
	}


	/**
	 * highligth search
	 */
	function highligthSearch(){
		if(typeof $("#inputAdminSearchCatalog").val() != 'undefined'){
			frags = $("#inputAdminSearchCatalog").val().split(" ");
			for(var i=0;i<frags.length;++i){
				$("#catalog-page-body").highlight(frags[i]);
			}
		}
	}

	function highligthSearchCatalogUnit(){
		if(typeof $("#inputAdminSearchCatalogUnit").val() != 'undefined'){
			frags = $("#inputAdminSearchCatalogUnit").val().split(" ");
			for(var i=0;i<frags.length;++i){
				$("#catalog-unit-page-body").highlight(frags[i]);
			}
		}
	}

	function selectAdminCatalogUnitsPage(pageIndex){
		solveCatalogPager(pageIndex);
		clearImageInGalleryHeader();
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseXML !== undefined){
						
						units = $(xhr.responseXML).find('unit');
						var html ='';
						if(units.length > 0){
							var indexCount =  pageIndex*itemCatalogPerPage;
							 for(var i = 0; i < units.length; ++i){
								 html = html+'<tr>';
								 var u = units[i];
								 var unitName =$(u).find('name');
								 var unitInfo =$(u).find('info');
								 var unitViewCount =$(u).find('views-count');
								
								 
								 var unitViews = $(u).find('view');
								 //check that!
								 $.merge(views,unitViews);

								 //views = $(u).find('view');
								 var packageName='';
								 var className = '';
								 if(unitViews.length >0){
									 var view0 = unitViews[0];
									 packageName =$(view0).find('package');
									 className =$(view0).find('class');
									 mountImageInGalleryHeader(i,packageName,className);
								 }
								 
								 html = html + '<td><small>'+indexCount+'</small></td>';
								 if(unitViews.length >0){
									 html = html + '<td><small><a href="#" rel="popover" data-trigger="hover" onmouseover="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').displayImage('+i+',\''+className.text()+'\',\''+packageName.text()+'\');return false;" id="tr_'+i+'" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openCatalogUnit(\''+unitName.text()+'\');return false;">'+unitName.text()+'</a></small></td>';	 
								 }
								 else{
									 html = html + '<td><small><a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openCatalogUnit(\''+unitName.text()+'\');return false;">'+unitName.text()+'</a></small></td>';
								 }
								 
								 html = html + '<td><small>'+unitInfo.text()+'</small></td>';
								 html = html + '<td><small> Total charts '+unitViewCount.text()+'</small></td>';
								 html = html + '</tr>';
								 indexCount = indexCount+1;
								

							 }
							 if(units.length < itemCatalogPerPage){
								 for(var i = units.length; i < itemCatalogPerPage; ++i){
									 html = html+'<tr><td>--</td><td>--</td><td>--</td><td>--</td></tr>';
								 }
							 }
						}else{
							html = html+'<tr><td><small><span class="boldorange">Result(0)</span></small></td><td><small>Search pattern matches any resources</small></td><td></td><td></td></tr>';
						}
						$("#catalog-page-header").html('<tr><th></th><th></th><th></th><th></th></tr><tr><th><small><span class="boldblue1">#</span><small></th><th><small><span class="boldblue1">Unit</span><small></th><th><small><span class="boldblue1">Unit Description</span><small></th><th><small><span class="boldblue1">Unit charts</span><small></th></tr>');
						$("#catalog-page-body").html(html);
						//alert("update page header");
						
						highligthSearch();
				}
			}
		};
		xhr.open("GET", getCatalogAPIContext()+'/units/page/'+pageIndex+'/viewsperpage/'+itemCatalogPerPage+'/search/'+encodeURIComponent($("#inputAdminSearchCatalog").val()), true);	
		xhr.send();	
	}


	function displayLabelMetaUnit(){
		$("#catalog-unit-name").html('<small><span class="colorblue1">'+$(selectedUnit).find('name').text()+'</span></small>');
	    $("#catalog-unit-desc").html('<small><span class="colorblue1">'+$(selectedUnit).find('info').text()+'</span></small>');
		$("#catalog-unit-views").html('<small><span class="colorblue1">'+$(selectedUnit).find('views-count').text()+'</span></small>');
	}

	function buildCatalogUnit(unit){

		$('#'+catalogContainer).load('catalog/user-catalog-unit-template.html',function() {		
				
				 	$('#catalogUnitTitle').text("Unit : "+unit);
				 
				 	displayTitle();
				 	
					//displayLabelMeta();
					displayLabelMetaUnit();

					installAdminCatalogUnitViews(unit);
		});
	}

	function openCatalogUnit(unit,loadContent){
		
		//bug
		if(loadContent !== false)
		contentBeforeAccessUnit  =  $('#content').html();
		 
		pageCatalogUnit = 0;
		pagesCatalogUnitFrameStart = 0;
		pagesCatalogUnitFrameEnd = 7;
		
		
		$(".popover").remove();
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseXML !== undefined){
					
					units = $(xhr.responseXML).find('unit');
					if(units.length > 0){
						 var u = units[0];
						 var unitName =$(u).find('name');
						 var unitInfo =$(u).find('info');
						 var unitViewCount =$(u).find('views-count');
						
						 selectedUnit = u;
						 buildCatalogUnit(unit);
						 
						 
					}
					else{
					}
					
				}
			}else{
			}
		};
		
		var service = getCatalogAPIContext()+'/unit/'+unit;
		xhr.open("GET", service, true);	
		xhr.send();
	}




	function installAdminCatalogUnitViews(unit){
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseText !== undefined){
					catalogUnitSize = parseInt(xhr.responseText);
					
					updateCatalogUnitLookupMessageLabelHeader();
					//pageCatalogUnit=0;				
					
					updateCatalogUnitPageLabelHeader();	
					
					selectPageUnitViews(unit,pageCatalogUnit);
					
				}
			}
		};
		
		var service = getCatalogAPIContext()+'/unit/'+unit+'/size/search/'+encodeURIComponent($("#inputAdminSearchCatalogUnit").val());
		xhr.open("GET", service, true);	
		xhr.send();
		
	}

	function selectPageUnitViews(unit,pageIndex){
		clearImageInGalleryHeader();
		if(pageIndex == pagesCatalogUnitFrameStart && pageIndex != 0){
			switchCatalogUnitFrame(-1);
		}
		else if(pageIndex == pagesCatalogUnitFrameEnd && pageIndex != pageCatalogUnitCount-1){
			switchCatalogUnitFrame(1);
		}
		pageCatalogUnit = pageIndex;
		
		countCatalogUnitPage();	
		
		buildCatalogUnitHTMLPager(unit);
		styleCatalogUnitActivePage();

		fetchUnitViews(unit,pageIndex);
		return false;
	}

	function fetchUnitViews(unit,pageIndex){
		
		var xhr = getConnector();		
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				if(xhr.responseXML !== undefined){
						views = $(xhr.responseXML).find('view');
						var html ='';
						if(views.length > 0){
							var indexCount =  pageIndex*itemCatalogUnitPerPage;
							 for(var i = 0; i < views.length; ++i){
								
								 var view = views[i];
								 var viewId =$(view).find('class');
								 var className =$(view).find('class');
								 var packageName =$(view).find('package');
								 var unitName =$(view).find('unit');
								 var description =$(view).find('description');
								 
								
								 mountImageInGalleryHeader(i,packageName,className);
								 
								 html = html+'<tr>';
								 html = html + '<td><small>'+indexCount+'</small></td>';
								 html = html + '<td><small><a href="#" rel="popover" data-trigger="hover" onmouseover="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').displayImage('+i+',\''+className.text()+'\',\''+packageName.text()+'\');return false;" id="tr_'+i+'" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openCatalogView(\''+className.text()+'\',\''+packageName.text()+'\',\'unit\');return false;">'+className.text()+'</a></small><br><span class="verysmall">'+packageName.text()+'</span></td>';
								 html = html + '<td><small>'+description.text()+'</small></td>';
								 html = html + '</tr>';
								 indexCount = indexCount+1;
							 }
							 if(views.length < itemCatalogUnitPerPage){
								 for(var i = views.length; i < itemCatalogUnitPerPage; ++i){
									 html = html+'<tr><td>--</td><td>--<br>--</td><td>--</td></tr>';
								 }
							 }
						}else{
							html = html+'<tr><td><small><span class="boldorange">Result(0)</span></small></td><td><small>search word matches any view name or package in the '+unit+' unit</small></td><td></td></tr>';
						}
						$("#catalog-unit-page-header").html('<tr><th></th><th></th><th></th></tr><tr><th><small><span class="boldblue1">#</span><small></th><th><small><span class="boldblue1">Chart</span><small></th><th><small><span class="boldblue1">Description</span><small></th></tr>');
						$("#catalog-unit-page-body").html(html);
						updateCatalogUnitPageLabelHeader();
						highligthSearchCatalogUnit();
				}
			}
		};
		xhr.open("GET", getCatalogAPIContext()+'/unit/'+unit+'/page/'+pageIndex+'/viewsperpage/'+itemCatalogUnitPerPage+'/search/'+encodeURIComponent($("#inputAdminSearchCatalogUnit").val()), true);	
		xhr.send();	
	}

	function displayLabelMetaView(viewClass,viewPackage){
		var v = getViewFromCache(viewClass,viewPackage);
		$("#view-class-name").text(viewClass);
		$("#view-package-name").text(viewPackage);
		$("#view-unit").text($(v).find('unit').text());
		$("#view-description").text($(v).find('description').text());
	}

	function openCatalogView(viewClass,viewPackage,caller){
		//alert("caller : "+caller);
		//on view open, came from charts or unit
		conf = caller;
		
		contentBeforeAccessView = $('#content').html();
		
		var catalogAPI = getCatalogAPIContext();
		
		var v = getViewFromCache(viewClass,viewPackage);
		//alert(toString(v));
		$('#'+catalogContainer).load('catalog/user-catalog-view-template.html',function() {		
			
			$(".popover").remove();
			
			displayTitle();
			
			//displayLabelMeta();
			displayLabelMetaView(viewClass,viewPackage);
			
			
			//demo global web start
			lookupView = viewPackage+'.'+viewClass;
			webStartHRef = getCatalogBase()+"/webstart/"+viewClass+'.jnlp';
			$("#view-webstart").html('<a href="'+webStartHRef+'">Web Start Frame</a>');
			
			var type = $(v).find('type').text();
			if(type == 'view'){
				
				$("#view-applet-embedded").html('<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').embedApplet(\'view\',\''+viewClass+'\',\''+viewPackage+'\');return false;">'+'Embeded Applet'+'</a>');
				$("#view-applet-full-page").html('<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').maxiMizeApplet(\'view\',\''+viewClass+'\',\''+viewPackage+'\');return false;">'+'Full Page Applet'+'</a>');
				
				var html2 = '<tr><td><small>View</small></td><td><small>'+'<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openViewSource(\''+viewClass+'\',\''+viewPackage+'\');">'+viewClass+'.java</a>'+'</small></td></tr>';
							 
				var sees = $(v).find('see');
				if(sees !== undefined && sees !== null && sees.length !== undefined && sees.length > 0){
					for (var s = 0; s < sees.length; s++) {
						var see = sees[s];
						var seeClass = $(see).find('see-class').text();
						var seePackage = $(see).find('see-package').text();
						html2 = html2 +'<tr><td><small>See</small></td><td><small>'+'<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openViewSource(\''+seeClass+'\',\''+seePackage+'\');">'+seeClass+'.java</a>'+'</small></tr></td>';
					}
				}
				$("#catalog-view-resources").html(html2);
				
				imageURL = getCatalogAPIContext()+'/view/'+encodeURIComponent(lookupView)+'/image/600/400';
				//imageHtml = '<img class="img-responsive" width="600" height="400" src="'+imageURL+'">';
				imageHtml = '<img class="img-responsive"  src="'+imageURL+'">';
				$("#catalog-view-image").html(imageHtml);
				
//				var x2dExtention = $(v).find('x2d').text();
//				if(x2dExtention === 'true'){
//					$('#x2dExtentionHolder').load('user-catalog-x2d-extention.html',function() {
//						var htmlX2D = '<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openX2DSource(\''+viewClass+'\',\''+viewPackage+'\');">'+'X2D XML source'+'</a>';
//						$("#view-x2d-source").html(htmlX2D);
//					});
//				}
//				
//				var captchas = $(v).find('captcha');
//				if(captchas !== undefined && captchas !== null && captchas.length>0){
//					$('#captchaExtentionHolder').load('user-catalog-captcha-extention.html',function() {
//						var html ='';
//						for (var c = 0; c < captchas.length; c++) {
//							var captcha = captchas[c];
//							var q = $(captcha).find('question').text();
//							var r = $(captcha).find('response').text();
//							html = html+'<tr><td><small>'+q+'</small></td><td><small>'+r+'</small></td></tr>';
//						}
//						$('#catalog-view-captcha-extention-body').html(html);
//					});
//				}else{
//				}
				
			}else{
				$("#view-applet-embedded").html('<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').embedApplet(\'dashboard\',\''+viewClass+'\',\''+viewPackage+'\');return false;">'+'Embeded Applet'+'</a>');
				$("#view-applet-full-page").html('<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').maxiMizeApplet(\'dashboard\',\''+viewClass+'\',\''+viewPackage+'\');return false;">'+'Full Page Applet'+'</a>');
				
				var xhr = getConnector();		
				xhr.onreadystatechange = function() {
					if (xhr.readyState == 4 && xhr.status == 200) {
						if(xhr.responseXML !== undefined){
							dashBoardviews = $(xhr.responseXML).find('view');
							var html ='';
							var html2 ='';
							if(dashBoardviews.length > 0){
								
								html = '';
								
								if(dashBoardviews.length == 2){
									console.log("dashboard has 2 views");
									var img1 = '<img class="img-responsive" width="600" height="250" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[0]).find('package').text()+'.'+$(dashBoardviews[0]).find('class').text())+'/image/600/250'+'">';
									var img2 = '<img class="img-responsive" width="600" height="250" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[1]).find('package').text()+'.'+$(dashBoardviews[1]).find('class').text())+'/image/600/250'+'">';
									html = '<table><tr><td>'+img1+'</td></tr><tr><td>'+img2+'</td></tr></table>';
								}
								if(dashBoardviews.length == 4){
									console.log("dashboard has 4 views");
									var img1 = '<img class="img-responsive" width="350" height="240" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[0]).find('package').text()+'.'+$(dashBoardviews[0]).find('class').text())+'/image'+'">';
									var img2 = '<img class="img-responsive" width="350" height="240" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[1]).find('package').text()+'.'+$(dashBoardviews[1]).find('class').text())+'/image'+'">';
									var img3 = '<img class="img-responsive" width="350" height="240" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[2]).find('package').text()+'.'+$(dashBoardviews[2]).find('class').text())+'/image'+'">';
									var img4 = '<img class="img-responsive" width="350" height="240" src="'+catalogAPI+'/view/'+encodeURIComponent($(dashBoardviews[3]).find('package').text()+'.'+$(dashBoardviews[3]).find('class').text())+'/image'+'">';
									html = '<table><tr><td>'+img1+'</td><td>'+img2+'</td></tr><tr><td>'+img3+'</td><td>'+img4+'</td></tr></table>';
								}
								
								$("#catalog-view-image").html(html);
								
								
								//resources
								html2 = '<tr><td><small>Dashboard</small></td><td><small>'+'<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openViewSource(\''+viewClass+'\',\''+viewPackage+'\');">'+viewClass+'.java</a>'+'</small></td></tr>';
								 for(var i = 0; i < dashBoardviews.length; ++i){
									var dashb = dashBoardviews[i];
									var dashViewClass = $(dashb).find('class').text();
									var dashViewPackage = $(dashb).find('package').text();
									html2 = html2 +'<tr><td><small>View</small></td><td><small>'+'<a href="#" onclick="getCatalogInstance(\''+catalogGroup+'\',\''+catalogArtifact+'\').openViewSource(\''+dashViewClass+'\',\''+dashViewPackage+'\');">'+dashViewClass+'.java</a>'+'</small></tr></td>';
								}
								 
								var sees = $(v).find('see');
								if(sees !== undefined && sees !== null && sees.length !== undefined && sees.length > 0){
//									for (var s = 0; s < sees.length; s++) {
//										var see = sees[s];
//										var seeClass = $(see).find('see-class').text();
//										var seePackage = $(see).find('see-package').text();
//										html2 = html2 +'<tr><td><small>See</small></td><td><small>'+'<a href="#" onclick="openViewSource(\''+seeClass+'\',\''+seePackage+'\');">'+seeClass+'.java</a>'+'</small></tr></td>';
//									}
								}
								$("#catalog-view-resources").html(html2);
								
								
							}else{
							}
						}
					}
				};
				xhr.open("GET", getCatalogAPIContext()+'/dashboard/'+viewPackage+"."+viewClass, true);	
				xhr.send();
			}
			
		});
	}

	function openReleaseNote(){
		contentBeforeAccessSource =  $('#content').html();
		$('#'+catalogContainer).load('catalog/user-catalog-view-source-template.html',function() {	
		 	$('#sourceTitle').html("Release note");
			var xhr = getConnector();
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4 && xhr.status == 200) {
					if(xhr.responseText !== undefined){
						$("#source-place-holder").html(xhr.responseText);
						prettyPrint();
					}
				}
			};
			xhr.open("GET", getCatalogAPIContext()+'/note/release',true);
		 	xhr.send();	
		});
	}

	function openLicenseNote(){
		contentBeforeAccessSource =  $('#content').html();
		$('#'+catalogContainer).load('catalog/user-catalog-view-source-template.html',function() {	
		 	$('#sourceTitle').html("License");
			var xhr = getConnector();
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4 && xhr.status == 200) {
					if(xhr.responseText !== undefined){
						$("#source-place-holder").html(xhr.responseText);
						prettyPrint();
					}
				}
			};
			xhr.open("GET", getCatalogAPIContext()+'/note/license',true);
		 	xhr.send();	
		});
	}


	

	function openViewSource(viewClass,viewPackage){
		contentBeforeAccessSource =  $('#content').html();
		lookupView = viewPackage+'.'+viewClass;
		$('#'+catalogContainer).load('catalog/user-catalog-view-source-template.html',function() {
			var xhr = getConnector();
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4 && xhr.status == 200) {
					if(xhr.responseText !== undefined){
						$("#source-place-holder").html(xhr.responseText);
						prettyPrint();
					}
				}
			};
			xhr.open("GET", getCatalogAPIContext()+'/view/'+lookupView+'/source',true);
		 	xhr.send();	
		});
	}

	function embedApplet(type,viewClass,viewPackage){
		$('#catalog-view-image').load('catalog/user-catalog-embeded-applet-template.html',function() {		
			
			var archives = "jensoft-core-"+CatalogDef.core+".jar"+","+CatalogDef.artifact+"-"+CatalogDef.version+".jar"+","+CatalogDef.artifact+"-"+CatalogDef.version+"-sources.jar";
			$("#uiapplet").attr('archive',archives);
			var appletCode;
			if(type === 'view'){
				appletCode = 'org.jensoft.core.catalog.ui.ViewAppletUI.class';
				$("#uiapplet").append('<param name="viewName" value="'+viewPackage+'.'+viewClass+'">');
			}
			else{
				appletCode = 'org.jensoft.core.catalog.ui.DashboardAppletUI.class';
				$("#uiapplet").append('<param name="dashboardName" value="'+viewPackage+'.'+viewClass+'">');
			}
			
			$("#uiapplet").attr('codebase',getCatalogBase()+"/resources");
			$("#uiapplet").attr('code',appletCode);
			$("#uiapplet").attr('width',"100%");
			$("#uiapplet").attr('height',"500px");
			$('#catalog-view-image').unbind('mousewheel');
			$('#'+catalogContainer).unbind('mousewheel');
			
			
			appletDivPlaceHolder = document.getElementById('catalog-view-image'); 
			appletDivPlaceHolder.onmousewheel = function(){ stopWheel(); }; /* IE7, IE8 */
			if(appletDivPlaceHolder.addEventListener){ /* Chrome, Safari, Firefox */
				appletDivPlaceHolder.addEventListener('DOMMouseScroll', stopWheel, false);
			}
			function stopWheel(e){
			    if(!e){ e = window.event; } /* IE7, IE8, Chrome, Safari */
			    if(e.preventDefault) { e.preventDefault(); } /* Chrome, Safari, Firefox */
			    e.returnValue = false; /* IE7, IE8 */
			}
		});	
	}


	function backFromSource(){
		$('#content').html(contentBeforeAccessSource);
		$(".popover").remove();
	}

	function backFromUnit(){
		$('#content').html(contentBeforeAccessUnit);
		$(".popover").remove();
	}

	function backFromView(){	
		 $('#content').html(contentBeforeAccessView);
		 $('#inputAdminSearchCatalog').val(catalogSearchPattern);
		 $('#inputAdminSearchCatalogUnit').val(catalogUnitSearchPattern);
		 $(".popover").remove();
	}

	function backFromApplet(){
		$(".popover").remove();
		$('#content').html(contentBeforeAccessApplet);
		$(".popover").remove();
	}

	
	
	function maxiMizeApplet(type,viewClass,viewPackage){
		contentBeforeAccessApplet = $('#content').html();
		$('#content').load('catalog/user-catalog-view-applet-large.html',function() {		
			
			var archives = "jensoft-core-"+CatalogDef.core+".jar"+","+CatalogDef.artifact+"-"+CatalogDef.version+".jar"+","+CatalogDef.artifact+"-"+CatalogDef.version+"-sources.jar";
			$("#uiapplet").attr('archive',archives);
			var appletCode;
			if(type === 'view'){
				appletCode = 'org.jensoft.core.catalog.ui.ViewAppletUI.class';
				$("#uiapplet").append('<param name="viewName" value="'+viewPackage+'.'+viewClass+'">');
			}

			else{
				appletCode = 'org.jensoft.core.catalog.ui.DashboardAppletUI.class';
				$("#uiapplet").append('<param name="dashboardName" value="'+viewPackage+'.'+viewClass+'">');
			}
			
			$("#uiapplet").attr('codebase',getCatalogBase()+"/resources");
			$("#uiapplet").attr('code',appletCode);
			
			$("#uiapplet").attr('width',"80%");
			$("#uiapplet").attr('height',"500px");
			
			$('#catalog-view-image').unbind('mousewheel');
			$('#'+catalogContainer).unbind('mousewheel');
			
			var h = $(window).height() - 60;    	
		    $('#uiapplet').attr( 'width', $(window).width()-100);
		    $('#uiapplet').attr( 'height', h );
		    
		    $(window).resize(function() {
		    	var hr = $(window).height() - 60;    	
		        $('#uiapplet').attr( 'width', $(window).width()-100);
		        $('#uiapplet').attr( 'height', hr );
		    });
		    
			appletDivPlaceHolder = document.getElementById('catalog-view-image'); 
			appletDivPlaceHolder.onmousewheel = function(){ stopWheel(); }; /* IE7, IE8 */
			if(appletDivPlaceHolder.addEventListener){ /* Chrome, Safari, Firefox */
				appletDivPlaceHolder.addEventListener('DOMMouseScroll', stopWheel, false);
			}
			function stopWheel(e){
			    if(!e){ e = window.event; } /* IE7, IE8, Chrome, Safari */
			    if(e.preventDefault) { e.preventDefault(); } /* Chrome, Safari, Firefox */
			    e.returnValue = false; /* IE7, IE8 */
			}
		});	
	}


	
	var publicCatalog = {
			openCatalog : openCatalog,
			
			openCatalogView : openCatalogView,
			openCatalogUnit : openCatalogUnit,
			
			switchCatalog : switchCatalog,
			
			selectCatalogPage : selectCatalogPage,
			selectPageUnitViews : selectPageUnitViews,
			
			
			embedApplet : embedApplet,
			maxiMizeApplet :maxiMizeApplet,
			
			openViewSource : openViewSource,
			openReleaseNote : openReleaseNote,
			openLicenseNote : openLicenseNote,
			
			searchCatalog : searchCatalog,
			searchCatalogUnit : searchCatalogUnit,
			
			displayImage : displayImage,
			displayDashboard : displayDashboard,
			
			backFromSource : backFromSource,
			backFromUnit :backFromUnit,
			backFromView : backFromView,
			backFromApplet:backFromApplet,
			
	};
	
	window["jensoft_cat_"+catalogGroup.replace('.','_')+'_'+catalogArtifact.replace('-','_')] = publicCatalog;
	return publicCatalog;

	
};

