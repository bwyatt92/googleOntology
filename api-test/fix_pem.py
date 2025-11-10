#!/usr/bin/env python3
"""
Fix PEM file formatting
Converts a single-line base64 key to proper PEM format with headers and line breaks
"""

import sys
import textwrap

def fix_pem_file(input_file, output_file):
    """Convert single-line base64 to proper PEM format"""

    # Read the file
    with open(input_file, 'r') as f:
        content = f.read().strip()

    # Remove any existing PEM headers/footers if present
    content = content.replace('-----BEGIN PRIVATE KEY-----', '')
    content = content.replace('-----END PRIVATE KEY-----', '')
    content = content.replace('-----BEGIN RSA PRIVATE KEY-----', '')
    content = content.replace('-----END RSA PRIVATE KEY-----', '')
    content = content.replace('\n', '')
    content = content.replace('\r', '')
    content = content.replace(' ', '')

    # Wrap at 64 characters per line (PEM standard)
    wrapped = textwrap.fill(content, width=64, break_long_words=True, break_on_hyphens=False)

    # Add PEM headers
    pem_content = f"""-----BEGIN PRIVATE KEY-----
{wrapped}
-----END PRIVATE KEY-----
"""

    # Write the fixed file
    with open(output_file, 'w') as f:
        f.write(pem_content)

    print(f"✅ Fixed PEM file written to: {output_file}")
    print(f"   Lines: {len(wrapped.split(chr(10)))}")
    print(f"   First line: {wrapped.split(chr(10))[0][:60]}...")
    print(f"   Last line: {wrapped.split(chr(10))[-1][-60:]}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_file = sys.argv[1]
        output_file = sys.argv[2] if len(sys.argv) > 2 else "fixed_" + input_file
    else:
        input_file = "pk.pem"
        output_file = "pk_fixed.pem"

    print(f"Fixing PEM file: {input_file} -> {output_file}")
    fix_pem_file(input_file, output_file)
