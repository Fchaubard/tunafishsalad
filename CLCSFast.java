import java.util.*;

public class CLCSFast {
	//
	// Setting these to false should compile out unwanted code.
	//
	
	// Sentinel values for important arrays, negative value fills,
	// so you can see which values got filled by printing afterwards.
	static final boolean DEBUG_SENTINELS = false;
	// Print arr after doing a subproblem.
	static final boolean DEBUG_PRINT_ARR_AFTERWARDS = false;
	// Some of the U,D,L,R limits will never be used if everything
	// is working correctly.
	static final boolean DEBUG_EXTRA_LIMITS = false;
	// When reverse-traversing the path after calculating the table,
	// print out messages explaining the path.
	static final boolean DEBUG_PRINT_PATH_RECOVERY_STORY = false;
	// When reverse-traversing the path, make sure that the
	// characters in the string match where we think they do!
	static final boolean DEBUG_CHECK_DIAGONAL_STRING_MATCH = true;
	// Print the LCS, defined by the bottom right table entry.
	static final boolean DEBUG_PRINT_LCS_EARLY = false;
	// Print the limits U,D,L,R after everything is done.
	static final boolean DEBUG_PRINT_LIMITS = false;


	
	static int[][] arr = new int[2*2048][2048];
	static char[] A, B; //String A and B
	static char[] Aext = new char[2*2048]; //2x repetition of string A ("A extended").
	static int m, n;
	
	// Path is stored as the implied table limits for subproblems above and below.
	// Limits are inclusive.
	// Array dimensions are [path][row or column][limit direction]
	//
	// For instance, to see the lowest element to include in column c in the
	// subproblem table above path p, look up path_lims_UD[p][c][U],
	// which is the largest index, since using inverted y coordinates,
	// ascending downwards).
	//
	// To find the right most element to include in the table underneath and left
	// of path p in row r, look up path_lims_LR[p][r][L].  This is numerically
	// largest index in this row.
	static int U = 0;
	static int D = 1;
	static int L = 0;
	static int R = 1; // right limit
	static int[][][] path_lims_UD = new int[2048 + 1][2*2048][2]; // Cache efficient for filling
	static int[][][] path_lims_LR = new int[2048 + 1][2*2048][2]; // Cache efficient for filling

	//Result array
	static int LCS_lengths[];
	
	static int CLCS() throws Exception {
		m = A.length;
		n = B.length;
		
		LCS_lengths = new int[m+1];
		
		// Make a 2x repeat of string A, call it Aext (A extended)
		{
		int j = 0;
		for(int t=0; t<2; t++)
			for(int i=0; i<m; i++)
				Aext[j++] = A[i];
		}
		
		// Initialize the path lengths to -1 for debugging, etc
		if(DEBUG_SENTINELS) {
			for(int i=0; i<=m; i++)
				LCS_lengths[i] = -1;
		}
		
		//
		// Initial boundaries
		// - Do an LCS search for upper/right bound
		// - then copy these m rows down for bottom/left bound
		
		// Path 0
		LCS_lengths[0] = First_LCS_PathFill(A,B);
		// Fill in missing table values for rows in the extend part of the table t
		for(int i=m+1; i<=2*m; i++) {
			path_lims_LR[0][i][R] = n+1; // Box out 
			path_lims_LR[0][i][L] = n; // Should never be used 
		}

		// Path m
		// Copy the U,D bounds to path m
		for(int i = 0; i<=n; i++) {
			path_lims_UD[m][i][U] = path_lims_UD[0][i][U] + m; 
			if(DEBUG_EXTRA_LIMITS)
				path_lims_UD[m][i][D] = path_lims_UD[0][i][D] + m; // Should never be used.
		}
		// Copy L,R values to path m, and also fill in any missing entries due extending table in A direction
		for(int i=0; i<m; i++) {
			path_lims_LR[m][i][R] = 0;
			if(DEBUG_EXTRA_LIMITS)
				path_lims_LR[m][i][L] = -1; // Box out - Should never be used 
		}
		for(int i=m; i<=2*m; i++) {
			path_lims_LR[m][i][R] = path_lims_LR[0][i-m][R]; 
			if(DEBUG_EXTRA_LIMITS)
				path_lims_LR[m][i][L] = path_lims_LR[0][i-m][L]; // Should never be used
		}

//		//TODO
//		//FIXME
//		// this just for debugging.
//		if(m==5)
//			return 0;

		//
		// Do recursive calls here:
		//
		FindShortestPaths(0,m);

		
		// Determine the longest of the LCSs to return.
		int maxlen=-1;
		for(int i=0; i<m; i++)
			if(	LCS_lengths[i] > maxlen)
				maxlen = LCS_lengths[i];
		
		return maxlen;
	}


	static int First_LCS_PathFill(final char[] A, final char[] B) throws Exception {
		//int m = A.length, n = B.length;
		int i, j;
		final int p = 0;
		
		for (i = 0; i <= m; i++)
			arr[i][0] = 0;
		for (j = 0; j <= n; j++)
			arr[0][j] = 0;

		for (i = 1; i <= m; i++) {
			for (j = 1; j <= n; j++) {
				arr[i][j] = Math.max(arr[i - 1][j], arr[i][j - 1]);
				if (Aext[i - 1] == B[j - 1])
					arr[i][j] = Math.max(arr[i][j], arr[i - 1][j - 1] + 1);
			}
		}

		// Debug: Print the whole table
		if(DEBUG_PRINT_ARR_AFTERWARDS) {
			System.out.println(p);
			debugPrintArr();
		}
		
		// Debug: Print the LCS length.
		if(DEBUG_PRINT_LCS_EARLY) {
			System.out.println("Path p = " + p + ", LCS = " + arr[m][n]);
		}

		//
		// Walk the path backwards from arr[m][n], and fill the limits accordingly
		//
		i = m; j = n;
		int currLCS;
		while(i >= 0 && j >= 0) {
			// Update the current table plateau height that we are trying to 
			// find the edge of, since this is the start of a new plateau
			currLCS = arr[i][j];

			//Set the L limit for this row.
			path_lims_LR[p][i][L] = j;
			
			// March Left throw rough looking for the table value to change
			// Set the upper and lower bounds in the columns we pass through
			while(j > 0 && arr[i][j-1] == currLCS) {
				if(DEBUG_PRINT_PATH_RECOVERY_STORY)
					System.out.println("Move left @ x = " + j + " y = " + i);
				//Update U,D,L,R
				path_lims_UD[p][j][U] = i; // Limit when coming from above is inclusive.
				path_lims_UD[p][j][D] = i; // Limit when coming from below is inclusive.
				j--;
			}

			//
			// Either a non-diagonal lower left corner of the path, or a diagonal, or (0,0).
			//
			
			//If lower left corner (not a diagonal), finish of the row, start the column.
			if(i > 0 && arr[i-1][j] == currLCS) {
				if(DEBUG_PRINT_PATH_RECOVERY_STORY)
					System.out.println("Move corn @ x = " + j + " y = " + i);
				//Set R to finish off row, U for column ends here
				path_lims_LR[p][i][R] = j; 
				path_lims_UD[p][j][U] = i;
				i--;
			}
			// Diagonal, or (0,0)
			else if((i > 0 && arr[i-1][j] != currLCS)
				    || (i == 0) )
			{
				//Strings should match here!
				if(DEBUG_PRINT_PATH_RECOVERY_STORY) {
					System.out.println("Move diag @ x = " + j + " y = " + i);
					//Strings should match here
					if(i!= 0 && j!=0) {
						System.out.print(Aext[i-1]);
						System.out.println(B[j-1]);
					}
				}
				if(DEBUG_CHECK_DIAGONAL_STRING_MATCH) {
					if(i!= 0 && j!=0)
						if(Aext[i-1] != B[j-1])
							throw(new Exception("Diagonal string mismatch!"));
				}
				
				//Update U,D,R, but not L since that was set at the beginning of the row
				// to the upper problem and lower problem
				path_lims_UD[p][j][U] = i;
				path_lims_UD[p][j][D] = i;
				path_lims_LR[p][i][R] = j;
				//Move one left, and one up, since this is a diagonal
				j--;
				i--;
				
				//Since just hit a diagonal or (0,0), loop iteration is done.
				continue;
			}
			
			//
			// If we made it this far, need to move upward in a column
			//
			
			// March up if not a diagonal
			while(i > 0 && arr[i-1][j] == currLCS) {
				if(DEBUG_PRINT_PATH_RECOVERY_STORY)
					System.out.println("Move up   @ x = " + j + " y = " + i);
				//Update L,R,  and U already set, D comes at diagonal
				path_lims_LR[p][i][L] = j;
				path_lims_LR[p][i][R] = j; 
				i--;
			}

			//
			// Diagonal (or [0,0] )
			//

			//Strings should match here!
			if(DEBUG_PRINT_PATH_RECOVERY_STORY) {
				System.out.println("Move diag @ x = " + j + " y = " + i);
				//Strings should match here
				if(i != 0 && j != 0) {
					System.out.print(Aext[i-1]);
					System.out.println(B[j-1]);
				}
			}
			if(DEBUG_CHECK_DIAGONAL_STRING_MATCH) {
				if(i != 0 && j != 0) {
					if(Aext[i-1] != B[j-1])
						throw(new Exception("Diagonal string mismatch!"));
				}
			}
			
			// Update D,L,R, since top of column. U was set at bottom of column
			// to the upper problem and lower problem
			path_lims_UD[p][j][D] = i;
			path_lims_LR[p][i][L] = j;
			path_lims_LR[p][i][R] = j;
			//Move one left, and one up, since this is a diagonal
			j--;
			i--;
		}
		
		return arr[m][n];
	}
	
	static void FindShortestPaths(int upper, int lower) throws Exception {
		// If there no more paths between upper and lower, we re done.
		if(lower - upper <= 1)
			return;
		
		int mid = (upper+lower)/2;

		//Split the problem by finding a path in the middle.
		LCS_lengths[mid] = SingleShortestPaths(mid, upper, lower);

		//Recursive calls subproblem bellow and above mid path.
		FindShortestPaths(upper,mid);
		FindShortestPaths(mid,lower);
	}
	
	static void debugPrintArr() {
		for(int j = 0; j<=2*m; j++) {
			for(int i=0; i<=n; i++) {
				System.out.print(String.format("% 3d ",arr[j][i]));
			}
			System.out.println();
		}
	}
	
	// Returns the LCS of this path, and fills in the table limits implied by this path.
	static int SingleShortestPaths(int mid, int upper, int lower) throws Exception {
		int i, j;
		final int p = mid;
		
		// For debugging purposes, fill the whole table with -2 sentinel so you can
		// see what got filled in this step.
		if(DEBUG_SENTINELS) {
			for(i=0; i<=2*m; i++)
				for(j=0; j<=n; j++)
					arr[i][j] = -2;
		}
		
		
		// Fill leftmost column in appropriate spots
		for(i = mid; i <= path_lims_UD[lower][0][U]; i++)
			arr[i][0] = 0;
		// Fill uppermost row in appropriate spots
		j = 0;
		while(j <= path_lims_LR[upper][mid][L]) {
			arr[mid][j] = 0;
			j++;
		}
		// Put zeros above every remaining column, so that the table iterator works as intended
		// on the top boundary. This is a good low-cost workaround.
		while(j <= n) {
			arr[(path_lims_UD[upper][j][D]-1)][j] = 0;
			j++;
		}
		
		//Traverse an m-wide band, starting at mid+1  (the zeroes row above it already full)
		for (i = mid + 1; i <= mid + m; i++) {
			//Traverse the row, starting at the left-limit imposed by the lower bounding path,
			//but no less than 1, because of the zero column
			int left_limit = (path_lims_LR[lower][i][R] > 1) ? path_lims_LR[lower][i][R] : 1;
			//...path ends at right-limit of row, which is fully defined by upper bounding path.
			int right_limit = path_lims_LR[upper][i][L];
			
			//Stick a zero in the first slot to the left of the range, such that the code
			// below works correctly on the left element when the left element is outside
			// of the range. This is a very efficient method to handle this.
			arr[i][left_limit - 1] = 0;
			for (j = left_limit; j <= right_limit; j++) {
				arr[i][j] = Math.max(arr[i - 1][j], arr[i][j - 1]);
				if (Aext[i - 1] == B[j - 1])
					arr[i][j] = Math.max(arr[i][j], arr[i - 1][j - 1] + 1);
			}
		}

		// Debug: Print the whole table
		if(DEBUG_PRINT_ARR_AFTERWARDS) {
			System.out.println(mid);
			debugPrintArr();
		}
		
		//Rewind to the bottom right corner.
		// - Bottom edge is limited by either mid + m, or the lower boundary bottom right corner.
		i = (path_lims_UD[lower][n][U] < mid + m)  ?  path_lims_UD[lower][n][U]  :  mid + m;
		j--;
		
		int LCS = arr[i][j];
		
		// Debug: Print the LCS length.
		if(DEBUG_PRINT_LCS_EARLY) {
			System.out.println("Path p = " + p + ", LCS = " + LCS);
		}

		//
		// Walk the path backwards from arr[i][j], and fill the limits accordingly
		//
		
		//First fill the L,R limits for rows above the band and below the band
		//Some of this may not be needed.
		for(int a=0; a<p; a++) {
			if(DEBUG_EXTRA_LIMITS)
				path_lims_LR[p][a][L] = -1; //Box it out, shouldn't ever be used.
			path_lims_LR[p][a][R] = 0;
		}
		for(int a=p+m+1; a<=2*m; a++) {
			path_lims_LR[p][a][L] = n;   
			if(DEBUG_EXTRA_LIMITS)
				path_lims_LR[p][a][R] = n+1; //Box it out, shouldn't ever be used.
		}
		
		
		int currLCS;
		while(i >= mid && j >= 0) {
			// Update the current table plateau height that we are trying to 
			// find the edge of, since this is the start of a new plateau
			currLCS = arr[i][j];

			//Set the L limit for this row.
			path_lims_LR[p][i][L] = j;

			// March Left throw rough looking for the table value to change
			// Set the upper and lower bounds in the columns we pass through
			while(j > 0 && arr[i][j-1] == currLCS) {
				if(DEBUG_PRINT_PATH_RECOVERY_STORY)
					System.out.println("Move left @ x = " + j + " y = " + i);
				//Update U,D,L,R
				path_lims_UD[p][j][U] = i; // Limit when coming from above is inclusive.
				path_lims_UD[p][j][D] = i; // Limit when coming from below is inclusive.
				j--;
			}

			//
			// Either a non-diagonal lower left corner of the path, or a diagonal, or (p,0) the upper left corner. 
			//

			//If lower left corner (not a diagonal), finish of the row, start the column.
			if(i > p && arr[i-1][j] == currLCS) {
				if(DEBUG_PRINT_PATH_RECOVERY_STORY)
					System.out.println("Move corn @ x = " + j + " y = " + i);
				//Set R to finish off row, U for column ends here
				path_lims_LR[p][i][R] = j; 
				path_lims_UD[p][j][U] = i;
				i--;
			}
			// Diagonal, or (p,0)
			else if((i > p && arr[i-1][j] != currLCS)
				    || (i == p) )
			{
				//Strings should match here!
				if(DEBUG_PRINT_PATH_RECOVERY_STORY) {
					System.out.println("Move diag @ x = " + j + " y = " + i);
					//Strings should match here
					if(i!= p && j!=0) {
						System.out.print(Aext[i-1]);
						System.out.println(B[j-1]);
					}
				}
				if(DEBUG_CHECK_DIAGONAL_STRING_MATCH) {
					if(i!= p && j!=0)
						if(Aext[i-1] != B[j-1])
							throw(new Exception("Diagonal string mismatch!"));
				}
				
				//Update U,D,R, but not L since that was set at the beginning of the row
				// to the upper problem and lower problem
				path_lims_UD[p][j][U] = i;
				path_lims_UD[p][j][D] = i;
				path_lims_LR[p][i][R] = j;
				//Move one left, and one up, since this is a diagonal
				j--;
				i--;
				
				//Since just hit a diagonal or (0,0), loop iteration is done.
				continue;
			}

			//
			// If we made it this far, need to move upward in a column
			//

			// March up if not a diagonal
			while(i > p && arr[i-1][j] == currLCS) {
				if(DEBUG_PRINT_PATH_RECOVERY_STORY)
					System.out.println("Move up   @ x = " + j + " y = " + i);
				//Update L,R,  and D already set, U comes at diagonal
				path_lims_LR[p][i][L] = j;
				path_lims_LR[p][i][R] = j; 
				i--;
			}

			//
			// Diagonal (or [0,0] )
			//
			//Strings should match here!
			if(DEBUG_PRINT_PATH_RECOVERY_STORY) {
				System.out.println("Move diag @ x = " + j + " y = " + i);
				//Strings should match here
				if(i != p && j != 0) {
					System.out.print(Aext[i-1]);
					System.out.println(B[j-1]);
				}
			}
			if(DEBUG_CHECK_DIAGONAL_STRING_MATCH) {
				if(i != 0 && j != 0) {
					if(Aext[i-1] != B[j-1])
						throw(new Exception("Diagonal string mismatch!"));
				}
			}

			//Update D,L,R, since top of column. U was set at bottom of column.
			// to the upper problem and lower problem
			path_lims_UD[p][j][D] = i;
			path_lims_LR[p][i][L] = j;
			path_lims_LR[p][i][R] = j;
			//Move one left, and one up, since this is a diagonal
			j--;
			i--;
		}
		
		return LCS;
	}
	
	
	public static void main(String[] args) throws Exception {
		Scanner s = new Scanner(System.in);
		int T = s.nextInt();
		for (int tc = 0; tc < T; tc++) {
			A = s.next().toCharArray();
			B = s.next().toCharArray();
			
			//
			// Fill the relevant parts of the limits arrays with a debug sentinel.
			//
			if(DEBUG_SENTINELS) {
				int m1 = A.length;
				int n1 = B.length;
				for(int p = 0; p <= m1; p++) {
					for(int j = 0; j <= ((2*m1 > n1) ? 2*m1: n1) ; j++) {
						path_lims_UD[p][j][0] = -2;
						path_lims_UD[p][j][1] = -2;
						path_lims_LR[p][j][0] = -2;
						path_lims_LR[p][j][1] = -2;
					}
				}
			}
			
			
			//
			// Call the algorithm
			//
			System.out.println(CLCS());

			//
			// Print the limits arrays for debugging purposes.
			//
			if(DEBUG_PRINT_LIMITS) {
				int m = A.length;
				int n = B.length;
				System.out.println("U");
				for(int p = 0; p <= m; p++) {
					for(int j = 0; j <= n; j++) {
						System.out.print(String.format("% 3d ", path_lims_UD[p][j][U]));
					}
					System.out.println();
				}
				System.out.println("D");
				for(int p = 0; p <= m; p++) {
					for(int j = 0; j <= n; j++) {
						System.out.print(String.format("% 3d ", path_lims_UD[p][j][D]));
					}
					System.out.println();
				}
				System.out.println("L");
				for(int p = 0; p <= m; p++) {
					for(int j = 0; j <= 2*m; j++) {
						System.out.print(String.format("% 3d ", path_lims_LR[p][j][L]));
					}
					System.out.println();
				}
				System.out.println("R");
				for(int p = 0; p <= m; p++) {
					for(int j = 0; j <= 2*m; j++) {
						System.out.print(String.format("% 3d ", path_lims_LR[p][j][R]));
					}
					System.out.println();
				}
			} //DEBUG_PRINT_LIMITS
		}
	}
}
