import java.util.*;

public class CLCSSlow {
	static int[][] arr = new int[2048][2048];
	static char[] A, B;

	static int CLCS() {
		char[] Atemp;
		int maxlen = 0;
		int m = A.length;
		Atemp = new char[m];
		
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

	static int LCS(final char[] A, final char[] B) {
		int m = A.length, n = B.length;
		int i, j;
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

		return arr[m][n];
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		int T = s.nextInt();
		for (int tc = 0; tc < T; tc++) {
			A = s.next().toCharArray();
			B = s.next().toCharArray();
			System.out.println(CLCS());
		}
	}
}
