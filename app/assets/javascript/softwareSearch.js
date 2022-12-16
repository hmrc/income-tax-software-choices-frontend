'use strict'

$(document).ready(function() {
  $('#searchTerm').on("input", searchEventHandler(500));

  $('.govuk-checkboxes__item').on("click", searchEventHandler(500));
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
      clearErrors();
      toggleLoading(true);
    },
    data: searchForm.serialize(),
    success: function(response) {
      const searchStatus = $(response).find('#updated-vendor-count').html()
      $('#vendor-count').html(searchStatus)
      $('#software-vendor-list').html(response);
      toggleLoading(false);
    },
    error: function() {
      searchForm.submit();
    }
  });
}

function toggleLoading(showLoading) {
  if(showLoading) {
    $('#software-vendor-list').css('opacity', 0)
    $('#software-vendors').addClass("loading");
  } else {
    $('#software-vendors').removeClass("loading");
    $('#software-vendor-list').css('opacity', 1)
  }
}

function clearErrors() {
  $('.govuk-error-summary').remove();
  $('.govuk-error-message').remove();
  $('.govuk-form-group--error').removeClass("govuk-form-group--error");

  if(document.title.split(": ").length > 1) {
    document.title = document.title.split(": ")[1];
  }
}
