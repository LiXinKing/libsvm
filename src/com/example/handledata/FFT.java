package com.example.handledata;

import android.R.integer;

public class FFT {
	/**
	 * @param args
	 */
	private double[] xConv;// ��x[n]���ж����Ƶ������еĽ��
	double[] Xr, Xi;
	public FFT(float[] x) throws Exception {
		// int[] x; ��Ų���ֵ������
		int n, m, j;
		n = x.length;
		m = 0;
		j = 1;

		if (n < 1 || n > 1024)
			throw new Exception("�������еĸ���С��1�������1024");// ��֤�������еĸ�������1��1024֮��
		for (int i = 1; i < 11; i++) {
			j *= 2;
			m++;
			if (j >= n)
				break;
		}

		xConv = new double[j]; // ��ʼ��xConv
		for (int i = 0; i < n; i++)
			xConv[i] = x[i];

		if (j > n) // xConv����
			for (int i = n; i < j; i++)
				xConv[i] = 0;
		i2Sort(xConv, m); // ��xConv���ж����Ƶ�������
		System.out.println("x[n]����" + x.length + '(' + m + "��)" + "������ֵ��"
				+ "�������Ϊ��" + (j - n) + '\n');
		myFFT(xConv, m);
	}
	private void i2Sort(double[] xConv2, int m) {
		int[] index = new int[xConv2.length]; // index�������ڣ���������
		int[] bits = new int[m];

		double[] temp = new double[xConv2.length];

		for (int i = 0; i < xConv2.length; i++)
			// xConv2��ԭ��ӳ��
			temp[i] = xConv2[i];

		for (int i = 0; i < index.length; i++) {
			index[i] = i; // ��i��λ�ã�����ǰ��ֵΪi
			for (int j = 0; j < m; j++) {
				bits[j] = index[i] - index[i] / 2 * 2; // ��ȡindex[i]�ĵ�jλ�����Ƶ�ֵ
				index[i] /= 2;
			}
			index[i] = 0; // �����i��λ�õ�ֵ
			for (int j = m, power = 1; j > 0; j--) {
				index[i] += bits[j - 1] * power; // ��i��λ�ã�������λ��
				power *= 2;
			}
			// System.out.println(index[i]); //����Ч��Ԥ��
		}

		for (int i = 0; i < xConv2.length; i++)
			// ����ʵ��
			xConv2[i] = temp[index[i]];

	}
	private void myFFT(double[] xConv2, int m) {
		int divBy; // divBy�ȷ�
		double[] Wr, Wi; // �ֱ��ʾ��FFT�����ʵ�����鲿����ת���ӵ�ʵ�����鲿
		double[] tempXr, tempXi; // ���ν���ݴ���
		int n = xConv2.length;
		double pi = Math.PI;
		divBy = 1;
		Xr = new double[n];
		Xi = new double[n];
		tempXr = new double[n];
		tempXi = new double[n];
		Wr = new double[n / 2];
		Wi = new double[n / 2];

		System.out.println("���������Ƶ������к��x[n]:");
		for (int i = 0; i < n; i++) { // ��ʼ��Xr��Xi��֮����������ʼ������Ϊ�˷�������ĵ��ν���ݴ�
			Xr[i] = xConv2[i];
			Xi[i] = 0;
			// System.out.println(String.format("%6.2f", xConv2[i]));
		}

		for (int i = 0; i < m; i++) { // ����Ҫ����m�ε��μ���
			divBy *= 2;
			for (int k = 0; k < divBy / 2; k++) { // ��ת���Ӹ�ֵ
				Wr[k] = Math.cos(k * 2 * pi / divBy);
				Wi[k] = -Math.sin(k * 2 * pi / divBy);
			}

			for (int j = 0; j < n; j++) { // ���ν���ݴ�
				tempXr[j] = Xr[j];
				tempXi[j] = Xi[j];
			}

			for (int k = 0; k < n / divBy; k++) { // �������㣺ÿһ�ֵ������㣬����n/2�Եĺ������룻n/2��Ϊn/divBy�飬ÿ��divBy/2����
				int wIndex = 0; // ��ת�����±�����
				for (int j = k * divBy; j < k * divBy + divBy / 2; j++) {
					double X1 = tempXr[j + divBy / 2] * Wr[wIndex]
							- tempXi[j + divBy / 2] * Wi[wIndex];
					double X2 = tempXi[j + divBy / 2] * Wr[wIndex]
							+ tempXr[j + divBy / 2] * Wi[wIndex];
					Xr[j] = tempXr[j] + X1;
					Xi[j] = tempXi[j] + X2;
					Xr[j + divBy / 2] = tempXr[j] - X1; // ���ζ�����Ա�������divBy/2
					Xi[j + divBy / 2] = tempXi[j] - X2;
					wIndex++;
					// System.out.println("j="+j);
				}
			}
		}

		/*
		 * System.out.println("FFT�����"); for(int i=0;i<n;i++) //FFT�����ʾ
		 * System.out.println(String.format("%6.2f",
		 * Xr[i])+" + j"+String.format("%6.2f",Xi[i])); }
		 */
		/*
		 * public static void main(String[] args) throws Exception{ float[] a =
		 * {1,-1,1,-1,2,1,1,2}; new FFT(a);s }
		 */
	}
	public float energy_cal() {
		float sum = 0;
		int n = Xr.length;
		for (int i = 0; i < n; i++) {
			sum += (float) (Xr[i] * Xr[i] + Xi[i] * Xi[i]);
		}
		float avr = sum / n;
		return avr;

	}

}
