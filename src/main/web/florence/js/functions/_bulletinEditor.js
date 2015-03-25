function bulletinEditor(collectionName, data){

  var newSections = [];
  var newTabs = [];
  var lastIndexSection, lastIndexTab;

  $('.fl-editor__headline').hide();
  $(".section-list").remove();
  $(".tab-list").remove();
  $("#metadata-list").remove();

  // Metadata load
  $("#metadata-section").append(
    '<div id="metadata-list">' +
    '<p>Title: <textarea class="auto-size" type="text" id="title" cols="40" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Next release: <textarea class="auto-size" type="text" id="nextRelease" cols="20" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Contact name: <textarea class="auto-size" type="text" id="contactName" cols="20" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Contact email: <textarea class="auto-size" type="text" id="contactEmail" cols="30" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Headline 1: <textarea class="auto-size" type="text" id="headline1" cols="40" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Headline 2: <textarea class="auto-size" type="text" id="headline2" cols="40" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Headline 3: <textarea class="auto-size" type="text" id="headline3" cols="40" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '<p>Summary: <textarea class="auto-size" type="text" id="summary" cols="40" style="box-sizing: border-box; min-height: 31px;"></textarea></p>' +
    '</div>');

  // Metadata edition and saving
  $("#title").val(data.title).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.title = $(this).val();
  });
  $("#nextRelease").val(data.nextRelease).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.nextRelease = $(this).val();
  });
  $("#contactName").val(data.contact.name).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.contact.name = $(this).val();
  });
  $("#contactEmail").val(data.contact.email).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.contact.email = $(this).val();
  });
  $("#summary").val(data.summary).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.summary = $(this).val();
  });
  $("#headline1").val(data.headline1).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.headline1 = $(this).val();
  });
  $("#headline2").val(data.headline2).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.headline2 = $(this).val();
  });
  $("#headline3").val(data.headline3).on('click keyup', function () {
    $(this).textareaAutoSize();
    data.headline3 = $(this).val();
  });

  // Edit sections
  // Load and edition
  $(data.sections).each(function(index, section){
    lastIndexSection = index + 1;
    var element = $('.fl-editor__sections').append(
        '<div id="' + index + '" class="section-list" style="background-color:grey; color:white;">' +
        '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
        'Title ' +
        '<textarea id="section__' + index + '" cols="50">' + section.title + '</textarea>' +
        '<textarea style="display: none;" id="section_markdown_' + index + '">' +
        section.markdown + '</textarea>' +
        '<button class="fl-panel--editor__sections__section-item__edit_' + index + '">Edit</button>' +
        '<button class="fl-panel--editor__sections__section-item__delete_' + index + '">Delete</button>' +
        '</div>').show();

    $(".fl-panel--editor__sections__section-item__edit_"+index).click(function() {
      editedValue = $("#section_markdown_" + index).val();

      var editorPrev = '<div style="float: right; margin-top: 50px; height:905px; overflow: scroll;" id="wmd-preview" class="wmd-panel wmd-preview"></div>';
      var editorEdit = '<div style="float: left; margin-top: 50px;" id="wmd-edit" class="wmd-panel">' +
      '<div id="wmd-button-bar"></div>' +
      '<textarea style="height:845px;" class="wmd-input" id="wmd-input">' + editedValue + '</textarea>' +
      '<button id="finish">Finish editing</button>' +
      '</div>';

      $('body').prepend(editorPrev, editorEdit);

      $("#finish").click(function(){
        editedText = $('#wmd-input').val();
        data.sections[index].markdown = editedText;
        $("#wmd-preview").remove();
        $("#wmd-edit").remove();
      });

      markdownEditor();
    });

    // Delete
    $(".fl-panel--editor__sections__section-item__delete_"+index).click(function() {
      $("#"+index).remove();
      data.sections.splice(index, 1);
      saveNewSection();
    });
  });

  //Add new sections
  if ($("#addSection").length === 0) {
    $(".fl-panel--editor__nav").prepend('<button id="addSection">Add new section</button>');
    $("#addSection").click(function () {
      $('.fl-editor__sections').append(
        '<div id="' + lastIndexSection + '" class="section-list" style="background-color:grey; color:white;">' +
          '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
          'Title ' +
          '<textarea id="section__' + lastIndexSection + '" cols="50"></textarea>' +
          '<textarea style="display: none;" id="section_markdown_' + lastIndexSection + '"></textarea>' +
          '<button class="fl-panel--editor__sections__section-item__edit_' + lastIndexSection + '">Edit</button>' +
          '<button class="fl-panel--editor__sections__section-item__delete_' + lastIndexSection + '">Delete</button>' +
        '</div>');
      sortableSections();
      saveNewSection();
    });
  }

  function saveNewSection() {
    var orderSection = $(".fl-editor__sections").sortable('toArray');
    $(orderSection).each(function(index, name){
      var title = $('#section__'+name).val();
      var markdown = $('#section_markdown_'+name).val();
      newSections[parseInt(index)] = {title: title, markdown: markdown};
    });
    data.sections = newSections;
    $(".section-list").remove();
    $("#metadata-list").remove();
    bulletinEditor(collectionName, data);
  }

  function sortableSections() {
    $(".fl-editor__sections").sortable();
  }
  sortableSections();

  // Edit accordion
  // Load and edition
  $(data.accordion).each(function(index, tab){
    lastIndexTab = index + 1;
    var element = $('.fl-editor__accordion').append(
        '<div id="' + index + '" class="tab-list" style="background-color:grey; color:white;">' +
        '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
        'Title ' +
        '<textarea id="tab__' + index + '" cols="50">' + tab.title + '</textarea>' +
        '<textarea style="display: none;" id="tab_markdown_' + index + '">' +
        tab.markdown + '</textarea>' +
        '<button class="fl-panel--editor__accordion__tab-item__edit_' + index + '">Edit</button>' +
        '<button class="fl-panel--editor__accordion__tab-item__delete_' + index + '">Delete</button>' +
        '</div>').show();

    $(".fl-panel--editor__accordion__tab-item__edit_"+index).click(function() {
      editedValue = $("#tab_markdown_" + index).val();

      var editorPrev = '<div style="float: right; margin-top: 50px; height:905px; overflow: scroll;" id="wmd-preview" class="wmd-panel wmd-preview"></div>';
      var editorEdit = '<div style="float: left; margin-top: 50px;" id="wmd-edit" class="wmd-panel">' +
          '<div id="wmd-button-bar"></div>' +
          '<textarea style="height:845px;" class="wmd-input" id="wmd-input">' + editedValue + '</textarea>' +
          '<button id="finish">Finish editing</button>' +
          '</div>';

      $('body').prepend(editorPrev, editorEdit);

      $("#finish").click(function(){
        editedText = $('#wmd-input').val();
        data.accordion[index].markdown = editedText;
        $("#wmd-preview").remove();
        $("#wmd-edit").remove();
      });

      markdownEditor();
    });

    // Delete
    $(".fl-panel--editor__accordion__tab-item__delete_"+index).click(function() {
      $("#"+index).remove();
      data.accordion.splice(index, 1);
      saveNewTab();
    });
  });

  //Add new tab
  if ($("#addTab").length === 0) {
    $(".fl-panel--editor__nav").prepend('<button id="addTab">Add new tab</button>');

    $("#addTab").click(function () {
      $('.fl-editor__accordion').append(
          '<div id="' + lastIndexTab + '" class="tab-list" style="background-color:grey; color:white;">' +
          '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
          'Title ' +
          '<textarea id="tab__' + lastIndexTab + '" cols="50"></textarea>' +
          '<textarea style="display: none;" id="tab_markdown_' + lastIndexTab + '"></textarea>' +
          '<button class="fl-panel--editor__accordion__tab-item__edit_' + lastIndexTab + '">Edit</button>' +
          '<button class="fl-panel--editor__accordion__tab-item__delete_' + lastIndexTab + '">Delete</button>' +
          '</div>');
      sortableTabs();
      saveNewTab();
    });
  }

  function saveNewTab() {
    var orderTab = $(".fl-editor__accordion").sortable('toArray');
    $(orderTab).each(function(index, name){
      var title = $('#tab__'+name).val();
      var markdown = $('#tab_markdown_'+name).val();
      newTabs[parseInt(index)] = {title: title, markdown: markdown};
    });
    data.accordion = newTabs;
    $(".tab-list").remove();
    $("#metadata-list").remove();
    bulletinEditor(collectionName, data);
  }

  function sortableTabs() {
    $(".fl-editor__accordion").sortable();
  }
  sortableTabs();

  // Save
  $('.fl-panel--editor__nav__save').unbind("click").click(function() {
    // Sections
    var orderSection = $(".fl-editor__sections").sortable('toArray');
    $(orderSection).each(function (indexS, nameS) {
      var title = $('#section__' + nameS).val();
      var markdown = data.sections[parseInt(nameS)].markdown;
      newSections[parseInt(indexS)] = {title: title, markdown: markdown};
    });
    data.sections = newSections;
    // Tabs
    var orderTab = $(".fl-editor__accordion").sortable('toArray');
    $(orderTab).each(function (indexT, nameT) {
      var title = $('#tab__' + nameT).val();
      var markdown = data.accordion[parseInt(nameT)].markdown;
      newTabs[parseInt(indexT)] = {title: title, markdown: markdown};
    });
    data.accordion = newTabs;

    updateContent(collectionName, JSON.stringify(data));
  });
}
