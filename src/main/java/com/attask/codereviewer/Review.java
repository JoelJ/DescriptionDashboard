package com.attask.codereviewer;

import hudson.model.User;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Date;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 2:23 PM
 */
@ExportedBean
public class Review {
	private final Date date;
	private final Status status;
	private final String message;
	private final User author;

	public Review(Date date, Status status, String message, User author) {
		this.date = date;
		this.status = status;
		this.message = message;
		this.author = author;
	}

    @Exported
    public String getId(){
        return date.toString();
    }
	@Exported
	public Date getDate() {
		return date;
	}

	@Exported
	public Status getStatus() {
		return status;
	}

	@Exported
	public String getMessage() {
		return message;
	}

	@Exported
	public User getAuthor() {
		if(author == null || author.getId() == null) { //it's odd, but having the SYSTEM user breaks the API. It's messed up!
			return null;
		}
		return author;
	}

	public static enum Status {
		Accepted, NotReviewed, Rejected
	}
}
