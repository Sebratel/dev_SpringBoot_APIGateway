import sys
import argparse

def main():
    parser = argparse.ArgumentParser(description='Remove user status.')
    parser.add_argument('--name', help='Name of the user')
    parser.add_argument('--cpf', help='CPF of the user')
    
    args = parser.parse_args()
    
    print(f"Removing status for user: Name={args.name}, CPF={args.cpf}")
    
    # Success exit code
    sys.exit(0)

if __name__ == "__main__":
    main()
