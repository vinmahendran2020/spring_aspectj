package com.dtcc.ion.utilities;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * A helper class to generate Pagination information for HTTP Link header.
 *
 * This public function generateHeadersForPage() will generate headers containing first,
 * next, previous and last navigation links, as well as information about page size, number, total
 * elemnts, and total pages.
 *
 * TODO: This class should belong in a separate utilities library.
 *
 */
public class PaginationLinkHeaderHelper {

  public static HttpHeaders generateHeadersForPage(Page page) {
    HttpHeaders responseHeaders = new HttpHeaders();

    // Adding metadata to header.
    responseHeaders.set("Page-Number", String.valueOf(page.getNumber()));
    responseHeaders.set("Page-Size", String.valueOf(page.getSize()));
    responseHeaders.set("Total-Elements", String.valueOf(page.getTotalElements()));
    responseHeaders.set("Total-Pages", String.valueOf(page.getTotalPages()));
    responseHeaders.set(
        "Access-Control-Expose-Headers",
        "Link,Page-Number,Page-Size,Total-Elements,Total-Pages"
    );

    responseHeaders.set("Link", getPaginationLinks(page));

    return responseHeaders;
  }

  private static String getPaginationLinks(Page page) {
    ServletUriComponentsBuilder uri = ServletUriComponentsBuilder.fromCurrentRequestUri();

    StringBuilder linkHeader = new StringBuilder();

    if (page.hasNext()) {
      String uriForNextPage = constructNextPageUri(uri, page);
      linkHeader.append(createLinkHeader(uriForNextPage, "next"));
    }
    // If a page has a previous, that means it also has a first.
    if (page.hasPrevious()) {
      appendCommaIfNecessary(linkHeader);
      String uriForPrvPage = constructPrevPageUri(uri, page);
      linkHeader.append(createLinkHeader(uriForPrvPage, "prev"));
      appendCommaIfNecessary(linkHeader);
      String uriForFirstPage = constructFirstPageUri(uri, page);
      linkHeader.append(createLinkHeader(uriForFirstPage, "first"));
    }

    if (hasLastPage(page)) {
      String uriForLastPage = constructLastPageUri(uri, page);
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(uriForLastPage, "last"));

    }

    return linkHeader.toString();
  }

  private static String createLinkHeader(String uri, String rel) {
    return "<" + uri + ">; rel=\"" + rel + "\"";
  }

  private static String constructNextPageUri(final UriComponentsBuilder uriBuilder, Page page) {
    return uriBuilder.replaceQueryParam("pageNum", page.getNumber() + 1)
        .replaceQueryParam("pageSize", page.getSize()).build().encode().toUriString();
  }

  private static String constructPrevPageUri(final UriComponentsBuilder uriBuilder, Page page) {
    return uriBuilder.replaceQueryParam("pageNum", page.getNumber() - 1)
        .replaceQueryParam("pageSize", page.getSize()).build().encode().toUriString();
  }

  private static String constructFirstPageUri(final UriComponentsBuilder uriBuilder, Page page) {
    return uriBuilder.replaceQueryParam("pageNum", 0).replaceQueryParam("pageSize", page.getSize())
        .build().encode().toUriString();
  }

  private static String constructLastPageUri(final UriComponentsBuilder uriBuilder, Page page) {
    return uriBuilder.replaceQueryParam("pageNum", page.getTotalPages() - 1)
        .replaceQueryParam("pageSize", page.getSize()).build().encode().toUriString();
  }

  private static boolean hasLastPage(Page page) {
    return page.getTotalPages() > 1 && page.hasNext();
  }

  private static void appendCommaIfNecessary(final StringBuilder linkHeader) {
    if (linkHeader.length() > 0) {
      linkHeader.append(", ");
    }
  }

}
