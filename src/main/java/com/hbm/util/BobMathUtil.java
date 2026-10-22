package com.hbm.util;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnegative;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.ToIntFunction;

public class BobMathUtil {

	//finally!
	public static int min(int... nums) {
		int smallest = Integer.MAX_VALUE;
		for(int num : nums) if(num < smallest) smallest = num;
		return smallest;
	}
	public static int max(int... nums) {
		int largest = Integer.MIN_VALUE;
		for(int num : nums) if(num > largest) largest = num;
		return largest;
	}
	public static long min(long... nums) {
		long smallest = Long.MAX_VALUE;
		for(long num : nums) if(num < smallest) smallest = num;
		return smallest;
	}
	public static long max(long... nums) {
		long largest = Long.MIN_VALUE;
		for(long num : nums) if(num > largest) largest = num;
		return largest;
	}
	public static float min(float... nums) {
		float smallest = Float.MAX_VALUE;
		for(float num : nums) if(num < smallest) smallest = num;
		return smallest;
	}
	public static float max(float... nums) {
		float largest = Float.MIN_VALUE;
		for(float num : nums) if(num > largest) largest = num;
		return largest;
	}
	public static double min(double... nums) {
		double smallest = Double.MAX_VALUE;
		for(double num : nums) if(num < smallest) smallest = num;
		return smallest;
	}
	public static double max(double... nums) {
		double largest = Double.MIN_VALUE;
		for(double num : nums) if(num > largest) largest = num;
		return largest;
	}

	public static double safeClamp(double val, double min, double max) {

		val = MathHelper.clamp_double(val, min, max);

		if(val == Double.NaN) {
			val = (min + max) / 2D;
		}

		return val;
	}

	public static Vec3 interpVec(Vec3 vec1, Vec3 vec2, float interp) {
		return Vec3.createVectorHelper(
				interp(vec1.xCoord,  vec2.xCoord, interp),
				interp(vec1.yCoord,  vec2.yCoord, interp),
				interp(vec1.zCoord,  vec2.zCoord, interp)
				);
	}

	public static double interp(double x, double y, float interp) { return x + (y - x) * interp; }
	public static double interp(double x, double y, double interp) { return x + (y - x) * interp; }

	public static double getAngleFrom2DVecs(double x1, double z1, double x2, double z2) {

		double upper = x1 * x2 + z1 * z2;
		double lower = Math.sqrt(x1 * x1 + z1 * z1) * Math.sqrt(x2 * x2 + z2 * z2);

		double result = Math.toDegrees(Math.cos(upper / lower));

		if(result >= 180)
			result -= 180;

		return result;
	}

	public static double getCrossAngle(Vec3 vel, Vec3 rel) {

		vel.normalize();
		rel.normalize();

		double vecProd = rel.xCoord * vel.xCoord + rel.yCoord * vel.yCoord + rel.zCoord * vel.zCoord;
		double bot = rel.lengthVector() * vel.lengthVector();
		double angle = Math.acos(vecProd / bot) * 180 / Math.PI;

		if(angle >= 180)
			angle -= 180;

		return angle;
	}

	public static Vec3 getDirectionFromAxisAngle(float pitch, float yaw, double length) {
		double ox = (double) (-MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * length);
		double oz = (double) (MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * length);
		double oy = (double) (-MathHelper.sin(pitch / 180.0F * (float) Math.PI) * length);

		return Vec3.createVectorHelper(ox, oy, oz);
	}

	public static float remap(float num, float min1, float max1, float min2, float max2){
		return ((num - min1) / (max1 - min1)) * (max2 - min2) + min2;
	}

	public static float remap01(float num, float min1, float max1){
		return (num - min1) / (max1 - min1);
	}

	public static float remap01_clamp(float num, float min1, float max1){
		return MathHelper.clamp_float((num - min1) / (max1 - min1), 0, 1);
	}

	public static ForgeDirection[] getShuffledDirs() {

		ForgeDirection[] dirs = new ForgeDirection[6];
		List<Integer> indices = new ArrayList<Integer>() {{ add(0); add(1); add(2); add(3); add(4); add(5); }};
		Collections.shuffle(indices);

		for(int i = 0; i < 6; i++) {
			dirs[i] = ForgeDirection.getOrientation(indices.get(i));
		}

		return dirs;
	}

	public static String toPercentage(float amount, float total) {
		return NumberFormat.getPercentInstance().format(amount / total);
	}

	public static String[] ticksToDate(long ticks) {

		int tickDay = 48000;
		int tickYear = tickDay * 100;

		final String[] dateOut = new String[3];
		long year = Math.floorDiv(ticks, tickYear);
		byte day = (byte) Math.floorDiv(ticks - tickYear * year, tickDay);
		float time = ticks - (tickYear * year + tickDay * day);
		time = (float) convertScale(time, 0, tickDay, 0, 10F);
		dateOut[0] = String.valueOf(year);
		dateOut[1] = String.valueOf(day);
		dateOut[2] = String.valueOf(time);
		return dateOut;
	}

	/**
	 * Rescale a number from one range to another
	 * @param toScale - The integer to scale
	 * @param oldMin - The current minimum value
	 * @param oldMax - The current maximum value
	 * @param newMin - The desired minimum value
	 * @param newMax - The desired maximum value
	 * @return The scaled number
	 */
	public static double convertScale(double toScale, double oldMin, double oldMax, double newMin, double newMax) {
		double prevRange = oldMax - oldMin;
		double newRange = newMax - newMin;
		return (((toScale - oldMin) * newRange) / prevRange) + newMin;
	}

	/**
	 * Rounds a number to so many significant digits
	 * @param num The number to round
	 * @param digits Amount of digits
	 * @return The rounded double
	 */
	public static double roundDecimal(double num, @Nonnegative int digits) {
		if(digits < 0)
			throw new IllegalArgumentException("Attempted negative number in non-negative field! Attempted value: " + digits);

		return new BigDecimal(num).setScale(digits, RoundingMode.HALF_UP).doubleValue();
	}
	
	/**
	 * @param amount
	 * @return the number as a string with thousand group commas
	 */
	public static String format(Number amount) {
		return String.format(Locale.US, "%,d", amount);
	}

	public static boolean getBlink() {
		return System.currentTimeMillis() % 1000 < 500;
	}

	public static String getShortNumber(long l) {
		
		double res;
		String suffix = "";
		long abs = Math.abs(l);

		if(abs >= Math.pow(10, 18)) {
			res = l / Math.pow(10, 18);
			suffix = "E";
		}
		else if(abs >= Math.pow(10, 15)) {
			res = l / Math.pow(10, 15);
			suffix = "P";
		}
		else if(abs >= Math.pow(10, 12)) {
			res = l / Math.pow(10, 12);
			suffix = "T";
		}
		else if(abs >= Math.pow(10, 9)) {
			res = l / Math.pow(10, 9);
			suffix = "G";
		}
		else if(abs >= Math.pow(10, 6)) {
			res = l / Math.pow(10, 6);
			suffix = "M";
		}
		else if(abs >= Math.pow(10, 3)) {
			res = l / Math.pow(10, 3);
			suffix = "k";
		}
		else {
			return Long.toString(l);
		}

		// Edgecase: a negative triple digit number would result in a 8 character long result so we will loose one decimal place
		if (res <= -100.0) {
			res = Math.round(res * 10.0) / 10.0;
		} else {
			res = Math.round(res * 100.0) / 100.0;
		}

		return res + suffix;
	}

	/**
	 * Adjusted sqrt, approaches standard sqrt but sqrt(x) is never bigger than x
	 *
	 *      ____________
	 *     /       1    |     1
	 * _  / x + ――――――――  - ―――――
	 *  \/      (x + 2)²    x + 2
	 *
	 * @param x
	 * @return
	 */
	public static double squirt(double x) {
		return Math.sqrt(x + 1D / ((x + 2D) * (x + 2D))) - 1D / (x + 2D);
	}

	/** A convenient way to re-define the value of pi, should the laws of nature change. */
	public static void setPi(double pi) {
		Field field = ReflectionHelper.findField(Math.class, "PI");
		try { field.setDouble(null, pi); } catch(Exception e) { }
	}

	public static double angularDifference(double alpha, double beta) {
		double delta = (beta - alpha + 180) % 360 - 180;
		return delta < -180 ? delta + 360 : delta;
	}

	// I am sick of trying to remember the ridiculous quirks of Java 8
	// so I wrote this thing that can shit any int-ish list-ish into a regular fucking int[]
	// made by mellow, thrown here by 70k
	public static int[] intCollectionToArray(Collection<Integer> in) {
		return intCollectionToArray(in, i -> (int)i);
	}

	public static int[] intCollectionToArray(Collection<Integer> in, ToIntFunction<? super Object> mapper) {
		return Arrays.stream(in.toArray()).mapToInt(mapper).toArray();
	}

	public static int[] collectionToIntArray(Collection<? extends Object> in, ToIntFunction<? super Object> mapper) {
		return Arrays.stream(in.toArray()).mapToInt(mapper).toArray();
	}

	public static int floor(double value) {
		int i = (int)value;
		return value < (double)i ? i - 1 : i;
	}

	public static long lfloor(double value) {
		long l = (long)value;
		return value < (double)l ? l - 1L : l;
	}

	public static double perlinFade(double value) {
		return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
	}

	public static double perlinFadeDerivative(double value) {
		return 30.0D * value * value * (value - 1.0D) * (value - 1.0D);
	}

	public static double lerp(double delta, double start, double end) {
		return start + delta * (end - start);
	}

	public static double clerp(double delta, double start, double end) {
		double angle = ((((end - start) % 360) + 540) % 360) - 180;
		return start + angle * delta;
	}

	public static double lerp2(double deltaX, double deltaY, double x0y0, double x1y0, double x0y1, double x1y1) {
		return lerp(deltaY, lerp(deltaX, x0y0, x1y0), lerp(deltaX, x0y1, x1y1));
	}

	public static double lerp3(double deltaX, double deltaY, double deltaZ, double x0y0z0, double x1y0z0, double x0y1z0, double x1y1z0, double x0y0z1, double x1y0z1, double x0y1z1, double x1y1z1) {
		return lerp(deltaZ, lerp2(deltaX, deltaY, x0y0z0, x1y0z0, x0y1z0, x1y1z0), lerp2(deltaX, deltaY, x0y0z1, x1y0z1, x0y1z1, x1y1z1));
	}

	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		} else {
			return value > max ? max : value;
		}
	}

	public static double getLerpProgress(double value, double start, double end) {
		return (value - start) / (end - start);
	}

	public static double lerpFromProgress(double lerpValue, double lerpStart, double lerpEnd, double start, double end) {
		return lerp(getLerpProgress(lerpValue, lerpStart, lerpEnd), start, end);
	}

	public static double clampedLerp(double start, double end, double delta) {
		if (delta < 0.0D) {
			return start;
		} else {
			return delta > 1.0D ? end : lerp(delta, start, end);
		}
	}

	public static void shuffleIntArray(int[] array) {
		Random rand = new Random();
		for(int i = array.length - 1; i > 0; i--) {
			int r = rand.nextInt(i + 1);
			int temp = array[r];
			array[r] = array[i];
			array[i] = temp;
		}
	}

	public static void reverseIntArray(int[] array) {
		int len = array.length;
		for(int i = 0; i < len / 2; i++) {
			int temp = array[i];
			array[i] = array[len - 1 - i];
			array[len - 1 - i] = temp;
		}
	}

	/** Soft peak sine */
	public static double sps(double x) {
		return Math.sin(Math.PI / 2D * Math.cos(x));
	}

	/** Square wave sine, make sure squarination is [0;1] */
	public static double sws(double x, double squarination) {
		double s = Math.sin(x);
		return Math.pow(Math.abs(s), 2 - squarination) / s;
	}

	/** randoms */
	public static int randIntBetween(Random rand, int min, int max) {
		if (min >= max) return min;
		return min + rand.nextInt(max - min);
	}


	public static double lambert_w (double x, int nb, int l) {
		//****************************************************************************80
		//
		// Purpose:
		//
		//    lambert_w() approximates the Lambert W function.
		//
		//  Discussion:
		//
		//    The call will fail if the input value X is out of range.
		//    The range requirement for the upper branch is:
		//      -exp(-1) <= X.
		//    The range requirement for the lower branch is:
		//      -exp(-1) < X < 0.
		//
		//  Licensing:
		//
		//    This code is distributed under the MIT license.
		//
		//  Modified:
		//
		//    30 August 2026
		//
		//  Author:
		//
		//    Original FORTRAN77 version by Andrew Barry, S. J. Barry,
		//    Patricia Culligan-Hensley.
		//    This version by John Burkardt.
		//    Here I am porting it to Java, Alex
		//
		//  Reference:
		//
		//    Andrew Barry, S. J. Barry, Patricia Culligan-Hensley,
		//    Algorithm 743: WAPR - A Fortran routine for calculating real
		//    values of the W-function,
		//    ACM Transactions on Mathematical Software,
		//    Volume 21, Number 2, June 1995, pages 172-181.
		//
		//  Input:
		//
		//    double x: the argument.
		//
		//    int nb: indicates the desired branch.
		//    * 0, the upper branch;
		//    * nonzero, the lower branch.
		//
		//    int l: indicates the interpretation of X.
		//    * 1, X is actually the offset from -(exp-1), so compute W(X-exp(-1)).
		//    * not 1, X is the argument; compute W(X);
		//
		//  Output:
		//
		//    double lambert_w: the approximate value of W(X).
		//
		double an2;
		double an3;
		double an4;
		double an5;
		double an6;
		double c13;
		double c23;
		double d12;
		double delx;
		double em;
		double em2;
		double em9;
		double eta;
		int i;
		int nbits;
		int niter;
		double reta;
		double s2;
		double s21;
		double s22;
		double s23;
		double t;
		double tb;
		double temp;
		double temp2;
		double ts;
		double value;
		double x0;
		double x1;
		double xx;
		double zl;
		double zn;

		niter = 1;

		nbits = 52;
		//
		//  Various mathematical constants.
		//
		em = -Math.exp ( -1.0 );
		em9 = -Math.exp ( -9.0 );
		c13 = 1.0 / 3.0;
		c23 = 2.0 * c13;
		em2 = 2.0 / em;
		d12 = -em2;
		tb = Math.pow ( 0.5, nbits );
		x0 = Math.pow ( tb, 1.0 / 6.0 ) * 0.5;
		x1 = ( 1.0 - 17.0 * Math.pow ( tb, 2.0 / 7.0 ) ) * em;
		an3 = 8.0 / 3.0;
		an4 = 135.0 / 83.0;
		an5 = 166.0 / 39.0;
		an6 = 3167.0 / 3549.0;
		s2 = Math.sqrt ( 2.0 );
		s21 = 2.0 * s2 - 3.0;
		s22 = 4.0 - 3.0 * s2;
		s23 = s2 - 2.0;

		if ( l == 1 ) {
			delx = x;

			if ( delx < 0.0 )
				return ( Double.NaN );

			xx = x + em;
		} else {
			if ( x < em ) {
				return ( Double.NaN );
			} else if ( x == em ) {
				value = -1.0;
				return value;
			}
			xx = x;
			delx = xx - em;
		}
		//
		//  Calculations for Wp.
		//
		if ( nb == 0 ) {
			if ( Math.abs ( xx ) <= x0 ) {
				value = xx / ( 1.0 + xx / ( 1.0 + xx / ( 2.0 + xx / ( 0.6 + 0.34 * xx ))));
				return value;
			} else if ( xx <= x1 ) {
				reta = Math.sqrt ( d12 * delx );
				value = reta / ( 1.0 + reta / ( 3.0 + reta / ( reta / ( an4 + reta / ( reta * an6 + an5 ) ) + an3 ) ) ) - 1.0;
				return value;
			} else if ( xx <= 20.0 ) {
				reta = s2 * Math.sqrt ( 1.0 - xx / em );
				an2 = 4.612634277343749 * Math.sqrt ( Math.sqrt ( reta + 1.09556884765625 ));
				value = reta / ( 1.0 + reta / ( 3.0 + ( s21 * an2 + s22 ) * reta / ( s23 * ( an2 + reta )))) - 1.0;
			} else {
				zl = Math.log ( xx );
				value = Math.log ( xx / Math.log ( xx / Math.pow ( zl, Math.exp ( -1.124491989777808 / ( 0.4225028202459761 + zl )) ) ));
			}
		} else {
			//
			//  Calculations for Wm.
			//
			if ( 0.0 <= xx ) {
				return ( Double.NaN );
			} else if ( xx <= x1 ) {
				reta = Math.sqrt ( d12 * delx );
				value = reta / ( reta / ( 3.0 + reta / ( reta / ( an4 + reta / ( reta * an6 - an5 ) ) - an3 ) ) - 1.0 ) - 1.0;
				return value;
			} else if ( xx <= em9 ) {
				zl = Math.log ( -xx );
				t = -1.0 - zl;
				ts = Math.sqrt ( t );
				value = zl - ( 2.0 * ts ) / ( s2 + ( c13 - t / ( 270.0 + ts * 127.0471381349219 )) * ts );
			} else {
				zl = Math.log ( -xx );
				eta = 2.0 - em2 * xx;
				value = Math.log ( xx / Math.log ( -xx / ( ( 1.0 - 0.5043921323068457 * ( zl + 1.0 ) ) * ( Math.sqrt ( eta ) + eta / 3.0 ) + 1.0 )));
			}
		}

		for ( i = 1; i <= niter; i++ ) {
			zn = Math.log ( xx / value ) - value;
			temp = 1.0 + value;
			temp2 = temp + c23 * zn;
			temp2 = 2.0 * temp * temp2;
			value = value * ( 1.0 + ( zn / temp ) * ( temp2 - zn ) / ( temp2 - 2.0 * zn ) );
		}

		return value;
	}
}
