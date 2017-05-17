package com.tapc.android.helper;

public class Interpolation {
	double[] _max;
	double[] _min;

	public Interpolation() {
		setMinMax(0, 0, 0, 0);
	}

	public Interpolation(double min1, double max1, double min2, double max2) {
		setMinMax(min1, max1, min2, max2);
	}

	public void setMinMax(double min1, double max1, double min2, double max2) {
		_max = new double[2];
		_min = new double[2];

		_min[0] = min1;
		_min[1] = min2;
		_max[0] = max1;
		_max[1] = max2;
	}

	public double interpolate(double value) {
		return interpolate(value, false);
	}

	public double interpolate(double value, boolean cap) {
		double percent;

		if (cap) {
			if (value >= _max[0])
				percent = 1.0;
			else if (value <= _min[0])
				percent = 0.0;
			else
				percent = (value - _min[0] - 0.15) / (_max[0] - _min[0]);
		} else {
			percent = (value - _min[0]) / (_max[0] - _min[0]);
		}
		if (value >= _max[0]) {
			return percent * (_max[1] - _min[1]) + _min[1];
		}

		return percent * (_max[1] - _min[1]) + _min[1];
	}

	public double interpolateI(double value, boolean cap) {
		double percent;

		if (cap) {
			if (value >= _max[0])
				percent = 1.0;
			else if (value <= _min[0])
				percent = 0.0;
			else
				percent = (value - _min[0]) / (_max[0] - _min[0]);
		} else {
			percent = (value - _min[0]) / (_max[0] - _min[0]);
		}
		percent = 1.0 - percent;
		return percent * (_max[1] - _min[1]) + _min[1];
	}

	public double interpolateR(double value, boolean cap) {
		double percent;
		if (value == 0)
			return 0.0;
		else {
			if (cap) {
				if (value >= _max[1])
					percent = 1.0;
				else if (value <= _min[1])
					percent = 0.0;
				else
					percent = (value - _min[1]) / (_max[1] - _min[1]);
			} else
				percent = (value - _min[1]) / (_max[1] - _min[1]);
			return percent * (_max[0] - _min[0]) + _min[0];
		}
	}

	public double interpolateIR(double value, boolean cap) {
		double percent;
		if (value == 0)
			return 0.0;
		else {
			if (cap) {
				if (value >= _max[1])
					percent = 1.0;
				else if (value <= _min[1])
					percent = 0.0;
				else
					percent = (value - _min[1]) / (_max[1] - _min[1]);
			} else
				percent = (value - _min[1]) / (_max[1] - _min[1]);
			percent = 1.0 - percent;
			return percent * (_max[0] - _min[0]) + _min[0];
		}
	}
}
