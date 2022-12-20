'use strict'

$(document).ready(function() {
  $('#searchTerm').on("input", searchEventHandler(500));
  $('#sortTerm').on("input", searchEventHandler(500));
});

function searchEventHandler(timeout) {
  /*
   * Debouncing
   * Wait for the user to stop typing before submitting the form.
   */
  let searchHandler;

  return () => {
    clearTimeout(searchHandler);
    searchHandler = setTimeout(submitSearch, timeout);
  };
}

function submitSearch() {
  const searchForm = $('form')
  const postAction = searchForm.attr('action') + "/ajax";

  $.ajax({
    dataType: "html",
    type: "POST",
    url: postAction,
    beforeSend: function() {
      toggleLoading(true);
    },
    data: searchForm.serialize(),
    success: function(response) {
      const searchStatus = $(response).find('#updated-glossary-result-count').html()
      $('#glossary-result-count').html(searchStatus)
      $('#glossary-list-content').html(response);
      toggleLoading(false);
    },
    error: function() {
      searchForm.submit();
    }
  });
}

function toggleLoading(showLoading) {
  if(showLoading) {
    $('#glossary-list-content').css('opacity', 0)
    $('#glossary-list-container').addClass("loading");
  } else {
    $('#glossary-list-container').removeClass("loading");
    $('#glossary-list-content').css('opacity', 1)
  }
}
