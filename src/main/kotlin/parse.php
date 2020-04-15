
	/**
	 * Replace single quotes with HTML markup
	 *
	 * @param string $text
	 *
	 * @return string The altered text
	 */
	private function handleAllQuotes( $text ) {
		$outtext = '';
		$lines = StringUtils::explode( "\n", $text );
		foreach ( $lines as $line ) {
			$outtext .= $this->doQuotes( $line ) . "\n";
		}
		$outtext = substr( $outtext, 0, -1 );
		return $outtext;
	}

	/**
	 * Helper function for handleAllQuotes()
	 *
	 * @param string $text
	 *
	 * @return string
	 * @internal
	 */
	public function doQuotes( $text ) {
		$arr = preg_split( "/(''+)/", $text, -1, PREG_SPLIT_DELIM_CAPTURE );
		$countarr = count( $arr );
		if ( $countarr == 1 ) {
			return $text;
		}

		// First, do some preliminary work. This may shift some apostrophes from
		// being mark-up to being text. It also counts the number of occurrences
		// of bold and italics mark-ups.
		$numbold = 0;
		$numitalics = 0;
		for ( $i = 1; $i < $countarr; $i += 2 ) {
			$thislen = strlen( $arr[$i] );
			// If there are ever four apostrophes, assume the first is supposed to
			// be text, and the remaining three constitute mark-up for bold text.
			// (T15227: ''''foo'''' turns into ' ''' foo ' ''')
			if ( $thislen == 4 ) {
				$arr[$i - 1] .= "'";
				$arr[$i] = "'''";
				$thislen = 3;
			} elseif ( $thislen > 5 ) {
				// If there are more than 5 apostrophes in a row, assume they're all
				// text except for the last 5.
				// (T15227: ''''''foo'''''' turns into ' ''''' foo ' ''''')
				$arr[$i - 1] .= str_repeat( "'", $thislen - 5 );
				$arr[$i] = "'''''";
				$thislen = 5;
			}
			// Count the number of occurrences of bold and italics mark-ups.
			if ( $thislen == 2 ) {
				$numitalics++;
			} elseif ( $thislen == 3 ) {
				$numbold++;
			} elseif ( $thislen == 5 ) {
				$numitalics++;
				$numbold++;
			}
		}

		// If there is an odd number of both bold and italics, it is likely
		// that one of the bold ones was meant to be an apostrophe followed
		// by italics. Which one we cannot know for certain, but it is more
		// likely to be one that has a single-letter word before it.
		if ( ( $numbold % 2 == 1 ) && ( $numitalics % 2 == 1 ) ) {
			$firstsingleletterword = -1;
			$firstmultiletterword = -1;
			$firstspace = -1;
			for ( $i = 1; $i < $countarr; $i += 2 ) {
				if ( strlen( $arr[$i] ) == 3 ) {
					$x1 = substr( $arr[$i - 1], -1 );
					$x2 = substr( $arr[$i - 1], -2, 1 );
					if ( $x1 === ' ' ) {
						if ( $firstspace == -1 ) {
							$firstspace = $i;
						}
					} elseif ( $x2 === ' ' ) {
						$firstsingleletterword = $i;
						// if $firstsingleletterword is set, we don't
						// look at the other options, so we can bail early.
						break;
					} elseif ( $firstmultiletterword == -1 ) {
						$firstmultiletterword = $i;
					}
				}
			}

			// If there is a single-letter word, use it!
			if ( $firstsingleletterword > -1 ) {
				$arr[$firstsingleletterword] = "''";
				$arr[$firstsingleletterword - 1] .= "'";
			} elseif ( $firstmultiletterword > -1 ) {
				// If not, but there's a multi-letter word, use that one.
				$arr[$firstmultiletterword] = "''";
				$arr[$firstmultiletterword - 1] .= "'";
			} elseif ( $firstspace > -1 ) {
				// ... otherwise use the first one that has neither.
				// (notice that it is possible for all three to be -1 if, for example,
				// there is only one pentuple-apostrophe in the line)
				$arr[$firstspace] = "''";
				$arr[$firstspace - 1] .= "'";
			}
		}

		// Now let's actually convert our apostrophic mush to HTML!
		$output = '';
		$buffer = '';
		$state = '';
		$i = 0;
		foreach ( $arr as $r ) {
			if ( ( $i % 2 ) == 0 ) {
				if ( $state === 'both' ) {
					$buffer .= $r;
				} else {
					$output .= $r;
				}
			} else {
				$thislen = strlen( $r );
				if ( $thislen == 2 ) {
					if ( $state === 'i' ) {
						$output .= '</i>';
						$state = '';
					} elseif ( $state === 'bi' ) {
						$output .= '</i>';
						$state = 'b';
					} elseif ( $state === 'ib' ) {
						$output .= '</b></i><b>';
						$state = 'b';
					} elseif ( $state === 'both' ) {
						$output .= '<b><i>' . $buffer . '</i>';
						$state = 'b';
					} else { // $state can be 'b' or ''
						$output .= '<i>';
						$state .= 'i';
					}
				} elseif ( $thislen == 3 ) {
					if ( $state === 'b' ) {
						$output .= '</b>';
						$state = '';
					} elseif ( $state === 'bi' ) {
						$output .= '</i></b><i>';
						$state = 'i';
					} elseif ( $state === 'ib' ) {
						$output .= '</b>';
						$state = 'i';
					} elseif ( $state === 'both' ) {
						$output .= '<i><b>' . $buffer . '</b>';
						$state = 'i';
					} else { // $state can be 'i' or ''
						$output .= '<b>';
						$state .= 'b';
					}
				} elseif ( $thislen == 5 ) {
					if ( $state === 'b' ) {
						$output .= '</b><i>';
						$state = 'i';
					} elseif ( $state === 'i' ) {
						$output .= '</i><b>';
						$state = 'b';
					} elseif ( $state === 'bi' ) {
						$output .= '</i></b>';
						$state = '';
					} elseif ( $state === 'ib' ) {
						$output .= '</b></i>';
						$state = '';
					} elseif ( $state === 'both' ) {
						$output .= '<i><b>' . $buffer . '</b></i>';
						$state = '';
					} else { // ($state == '')
						$buffer = '';
						$state = 'both';
					}
				}
			}
			$i++;
		}
		// Now close all remaining tags.  Notice that the order is important.
		if ( $state === 'b' || $state === 'ib' ) {
			$output .= '</b>';
		}
		if ( $state === 'i' || $state === 'bi' || $state === 'ib' ) {
			$output .= '</i>';
		}
		if ( $state === 'bi' ) {
			$output .= '</b>';
		}
		// There might be lonely ''''', so make sure we have a buffer
		if ( $state === 'both' && $buffer ) {
			$output .= '<b><i>' . $buffer . '</i></b>';
		}
		return $output;
	}