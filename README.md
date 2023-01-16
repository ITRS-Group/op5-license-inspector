# op5-license-inspector

Use with an OP5 lic file to print some useful information. Grab the package or binary of choice from [the latest release page](https://github.com/ITRS-Group/op5-license-inspector/releases/latest).

## Example

``` sh
$ op5-license-inspector /etc/op5license/op5license.lic
Recipient:    jthoren@itrsgroup.com
Company:      ITRS Group
Customer ID:  0036M00003GCTn7QAH

Valid from:   2022-01-01
Valid to:     2024-01-01

License type: enterprise_plus

Hosts:        1
Services:     1
Peers:        1
Pollers:      1

Logger:       true
Trapper:      true
NagVis:       true
BSM:          true
Reports:      true

Custom data:  {"License details" "For internal development purposes only"}
```

## License

```
MIT License

Copyright (c) 2022 ITRS Group Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
