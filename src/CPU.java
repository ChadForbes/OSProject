
public class CPU 
{
	public int opCode;
	public int instructionType;
	
	public int SReg1;
	public int SReg2;
	public int DstReg;
	public int BReg;
	public int address;
	public int reg1;
	public int reg2;
	
	public int programCounter;
	public String[] cache;
	
//	public String fetch(int progCounter)
//	{
//		//String instruction = cache[progCounter];
//		//return instruction;
//	}
	

	
	public int decode(String instruction) 
	{
		String binInString = Conversions.hexStringToBinaryString(instruction);
		String tempInString = binInString;
		
		instructionType = Conversions.binaryStringToLiteralInteger(tempInString.substring(0, 2));
		opCode = Conversions.binaryStringToLiteralInteger(tempInString.substring(2, 8));
		
		switch(instructionType) 
		{
		case 00:
		{
			SReg1 = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			SReg2 = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			DstReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(16, 20));
			break;
		}
		case 01:
		{
			BReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			DstReg = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			address = Conversions.binaryStringToLiteralInteger(tempInString.substring(16));
			break;
		}
		case 10:
		{
			address = Conversions.binaryStringToLiteralInteger(tempInString.substring(8));
			break;
		}
		case 11:
		{
			reg1 = Conversions.binaryStringToLiteralInteger(tempInString.substring(8, 12));
			reg2 = Conversions.binaryStringToLiteralInteger(tempInString.substring(12, 16));
			address = Conversions.binaryStringToLiteralInteger(tempInString.substring(16));
			break;
		}
		default:
		{
			System.out.println("EXCEPTION: Invalid instruction type");
		}
		}
		return opCode;
	}

}
