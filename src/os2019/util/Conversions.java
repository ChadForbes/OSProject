package os2019.util;

public class Conversions 
{
	public static int hexToDecimal(String hex) 
	{
		hex = hex.trim();
		return Integer.parseUnsignedInt(hex, 16);
	}
	
	public static String decimalToHex(int decimal) 
	{
		return Integer.toHexString(decimal);
	}
	
	public static String hexStringToBinaryString(String hex) 
	{
		hex = hex.trim();
		int i = Integer.parseUnsignedInt(hex, 16);
		String binaryString = Integer.toBinaryString(i);
		
		while (binaryString.length() != 32) 
		{
			binaryString = "0" + binaryString;
		}
		
		return binaryString;
	}
	
	public static int binaryStringToInteger(String binary) 
	{
		binary = binary.trim();
		int retVal = Integer.parseUnsignedInt(binary, 2);
		return retVal;
	}
		
}
