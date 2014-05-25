import java.util.*;

public class CLCSFast {
	static int[][] arr = new int[2048][2048];
	static char[] A, B;
	
	// Path is stored as the implied table limits for subproblems above and below.
	// Limits are inclusive.
	// Array dimensions are [path][row or column][limit direction]
	//
	// For instance, to see the lowest element to include in column c in the
	// subproblem table above path p, look up path_lims_UD[p][c][U],
	// which is the largest index, since using inverted y coordinates,
	// ascending downwards).
	//
	// To find the right most element to include in the table underneith and left
	// of path p in/ row r, look up path_lims_LR[p][r][L].  This is numerically
	// largest index in this row.
	static int U = 0;
	static int D = 1;
	static int L = 0;
	static int R = 1; // right limit
	static int[][][] path_lims_UD = new int[2*2048][2048][2]; // Cache efficient for filling
	static int[][][] path_lims_LR = new int[2*2048][2048][2]; // Cache efficient for filling

	static int CLCS() {
		int m = A.length;
		int n = B.length;
		
		int LCS_lengths[] = new int[m+1];
		
		char[] Aext = new char[2*m];
		
		// Make a 2x repeat of A, call it Aext (A extended)
		{
		int j = 0;
		for(int t=0; t<2; t++)
			for(int i=0; i<m; i++)
				Aext[j++] = A[i];
		}
		
		// Initialize the path lengths to -1 for debugging, etc
		for(int i=0; i<=m; i++)
			LCS_lengths[i] = -1;
		
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
			path_lims_UD[m][i][D] = path_lims_UD[0][i][D] + m; // Should never be used.
		}
		// Copy L,R values to path m, and also fill in any missing entries due extending table in A direction
		for(int i=0; i<m; i++) {
			path_lims_LR[m][i][R] = 0; 
			path_lims_LR[m][i][L] = -1; // Box out - Should never be used 
		}
		for(int i=m; i<=2*m; i++) {
			path_lims_LR[m][i][R] = path_lims_LR[0][i-m][R]; 
			path_lims_LR[m][i][L] = path_lims_LR[0][i-m][L]; // Should never be used
		}

		
		// Do recursive calls here:
		// TODO
		// TODO
		// TODO
		// TODO
		
	
		
		
		// Determine the longest of the LCSs to return.
		int maxlen=-1;
		for(int i=0; i<m; i++)
			if(	LCS_lengths[i] > maxlen)
				maxlen = LCS_lengths[i];
		
		return maxlen;
	}


	static int First_LCS_PathFill(final char[] A, final char[] B) {
		int m = A.length, n = B.length;
		int i, j;
		final int p = 0;
		
		for (i = 0; i <= m; i++)
			arr[i][0] = 0;
		for (j = 0; j <= n; j++)
			arr[0][j] = 0;

		for (i = 1; i <= m; i++) {
			for (j = 1; j <= n; j++) {
				arr[i][j] = Math.max(arr[i - 1][j], arr[i][j - 1]);
				if (A[i - 1] == B[j - 1])
					arr[i][j] = Math.max(arr[i][j], arr[i - 1][j - 1] + 1);
			}
		}

		// Print the number of matches.
		System.out.println("Matches = " + arr[m][n]);

		//
		// Walk the path backwards from arr[m][n], and fill the limits accordingly
		//
		i = m; j = n;
		int currLCS;
		while(i >= 0 && j >= 0) {
			// Update the current table plateau height that we are trying to 
			// find the edge of, since this is the start of a new plateau
			currLCS = arr[i][j];
			
			// March Left throw rough looking for the table value to change
			// Set the upper and lower bounds in the columns we pass through
			while(j > 0 && arr[i][j-1] == currLCS) {
				System.out.println("Move left @ x = " + j + " y = " + i);
				//Update U,D,L,R
				path_lims_UD[p][j][U] = i;   // Limit when coming from above is inclusive.
				path_lims_UD[p][j][D] = i+1; // Limit when coming from below is non-inclusive
				j--;
			}

			//
			// Either a non-diagonal lower left corner of the path, or a diagonal. 
			// - No special handler needed

			// March up if not a diagonal
			while(i > 0 && arr[i-1][j] == currLCS) {
				System.out.println("Move up   @ x = " + j + " y = " + i);
				//Update L,R,  and D already set, U comes at diagonal
				path_lims_LR[p][i][L] = j;    // Limit when coming from left is inclusive.
				path_lims_LR[p][i][R] = j+1;  // Limit when coming from right is non-inclusive 
				i--;
			}

			//
			// Diagonal (or [0,0] )
			//

			//Strings should match here!
			System.out.println("Move diag @ x = " + j + " y = " + i);
			//Strings should match here
			if(i != 0 && j != 0) {
				System.out.print(A[i-1]);
				System.out.println(B[j-1]);
			}
			
			//Update U,D,L,R, which must all equal this square, since it is a diagonal, thus inclusive
			// to the upper problem and lower problem
			path_lims_UD[p][j][U] = i;
			path_lims_UD[p][j][D] = i;
			path_lims_LR[p][i][L] = j;
			path_lims_LR[p][i][R] = j;
			//Move one left, and one up, since this is a diagonal
			j--;
			i--;
		}
		
		return arr[m][n];
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		int T = s.nextInt();
		for (int tc = 0; tc < T; tc++) {
			A = s.next().toCharArray();
			B = s.next().toCharArray();
			
			//
			// Fill the relevent parts of the limits arrays with a debug sentinel.
			//
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
					
			//
			// Call the algorithm
			//
			System.out.println(CLCS());

			//
			// Print the limits arrays for debugging purposes.
			//
			int m = A.length;
			int n = B.length;
			System.out.println("U");
			for(int p = 0; p <= m; p++) {
				for(int j = 0; j <= n; j++) {
					System.out.print(path_lims_UD[p][j][U]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("D");
			for(int p = 0; p <= m; p++) {
				for(int j = 0; j <= n; j++) {
					System.out.print(path_lims_UD[p][j][D]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("L");
			for(int p = 0; p <= m; p++) {
				for(int j = 0; j <= 2*m; j++) {
					System.out.print(path_lims_LR[p][j][L]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("R");
			for(int p = 0; p <= m; p++) {
				for(int j = 0; j <= 2*m; j++) {
					System.out.print(path_lims_LR[p][j][R]);
					System.out.print(" ");
				}
				System.out.println();
			}
			
		}
	}
}
