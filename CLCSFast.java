import java.util.*;

public class CLCSFast {
	static int[][] arr = new int[2048][2048];
	static char[] A, B;
	
	//Path is stored as the implied table limits for subproblems above and below.
	//Limits are inclusive.
	//Array dimensions are [path][row or column][limit direction]
	//For instance, to see the lowest element below the path p in column c,
	// look up path_lims_UD[p][c][U], and that is the upper limit of the search (numerically
	// smallest index, since using inverted y coordinates, ascending downwards).
	// To find the right most element to include in the table underneith path p in
	// row r, look up path_lims_LR[p][r][R].  This is numerically largest index in this row.
	static int U = 0;
	static int D = 1;
	static int L = 0;
	static int R = 1; // right limit
	static int[][][] path_lims_UD = new int[2*2048][2048][2]; // Cache efficient for filling
	static int[][][] path_lims_LR = new int[2*2048][2048][2]; // Cache efficient for filling

/*	static int CLCS() {
		int m = A.length;
		int n = B.length;
		
		
		
		//Try each LCS(cut(A,k),cut(B,0))
		for(int k = 0; k < m; k++) {
			// Cut A
			int Atemp_ind = 0;
			for(int i = k; i < m; i++)
				Atemp[Atemp_ind++] = A[i];
			for(int i = 0; i < k; i++)
				Atemp[Atemp_ind++] = A[i];
			
			//Calculate LCS for this cut of A
			int thislen = LCS(Atemp,B);

			//Update the maximum LCS length, if we found a new max.
			if(thislen> maxlen)
				maxlen = thislen;
		}
		
		return maxlen;
	}
*/

	static int LCS_PathFill(final char[] A, final char[] B) {
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
			
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					path_lims_UD[i][j][0] = -1;
					path_lims_UD[i][j][1] = -1;
					path_lims_LR[i][j][0] = -1;
					path_lims_LR[i][j][1] = -1;
				}
			}
					
			
			LCS_PathFill(A,B);
			//System.out.println(CLCS());

			System.out.println("U");
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					System.out.print(path_lims_UD[i][j][U]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("D");
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					System.out.print(path_lims_UD[i][j][D]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("L");
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					System.out.print(path_lims_LR[i][j][L]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("R");
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					System.out.print(path_lims_LR[i][j][R]);
					System.out.print(" ");
				}
				System.out.println();
			}
			
		}
	}
}
