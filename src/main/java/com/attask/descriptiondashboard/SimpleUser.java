package com.attask.descriptiondashboard;

import java.io.Serializable;

/**
 * User: Joel Johnson
 * Date: 9/19/12
 * Time: 10:31 AM
 */
public class SimpleUser implements Serializable {
	private final String id;
	private final String name;

	public SimpleUser(String id, String name) {
		if(id == null || id.isEmpty()) {
			throw new IllegalArgumentException("id cannot be null or empty");
		}
		if(name == null || name.isEmpty()) {
			id = name;
		}

		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SimpleUser)) return false;

		SimpleUser that = (SimpleUser) o;
		return !(id != null ? !id.equals(that.id) : that.id != null);

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
