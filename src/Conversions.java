// Author Ryan Wheeler
import java.lang.Math;

public class Conversions 
{
	// method to convert a hex string to a binary int
	public static int hexToDecimal(String hex) 
	{
		//Long i = Long.parseLong(hex, 16);

		//int j = Math.toIntExact(i);
		//return j;
		return Integer.parseInt(hex, 16);
	}
	
	// method to convert a decimal int to a hex string
	public static String decimalToHex(int decimal) 
	{
		return Integer.toHexString(decimal);
	}
	
	// method to convert a hex string to a binary string
	public static String hexStringToBinaryString(String hex) 
	{
		Long i = Long.parseLong(hex, 16);

		String binaryString = Long.toBinaryString(i);
		
		// add leading 0's to match address length (32-bits)
		while (binaryString.length() != 32) 
		{
			binaryString = "0" + binaryString;
		}
		
		return binaryString;
	}
	
	// method to convert a binary string to a decimal value (literal integer)
	public static int binaryStringToLiteralInteger(String binary) 
	{
		int retVal = Integer.parseInt(binary, 2);
		return retVal;
	}
		
}
