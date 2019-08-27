/**
 * 
 */
package com.winwin.winwin.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
@Getter
@Setter
public class SlackMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String filename;
	private String filetype;
	private String username;
	private String content;
	private String initial_comment;
	private String channels;
	private String text;
	private String as_user;
	private String channel;

}
