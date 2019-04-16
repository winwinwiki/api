/**
 * 
 */
package com.winwin.winwin.exception;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ExceptionResponse {

	String errorMessage;
	HttpStatus statusCode;

}
