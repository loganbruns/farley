// Base64 decoder
// @(#)$Id$

var b64chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

var decode_s, decode_idx;

function getNibble() {
  var result = -1;

  do {
    if (decode_idx == decode_s.length) break;
    result = b64chars.indexOf(decode_s.charAt(decode_idx));
    decode_idx++;
  } while (result < 0);

  return result;
}

exports.decode = function(s) {
  var buf = new Array(3);
  var r = "";
  var l = 0;
  decode_s = s;
  decode_idx = 0;
  do {
    i = 0;
    if (((a = getNibble()) < 0) ||
        ((b = getNibble()) < 0) ||
        ((c = getNibble()) < 0) ||
        ((d = getNibble()) < 0))
      break;
      if (a != 64)
        buf[0] = (a << 2);
      if (b != 64) {
        buf[0] |= ((b & 0xf0) >> 4);
        buf[1] = ((b & 0x0f) << 4);
        i++;
      }
      if (a != 64)
        r = r + String.fromCharCode(buf[0]);
      if (c != 64) {
        buf[1] |= ((c & 0xfc) >> 2);
        buf[2] = ((c & 3) << 6);
        i++;
      }
      if (b != 64)
        r += String.fromCharCode(buf[1]);
      if (d != 64) {
        buf[2] |= d;
        i++;
      }
      if (c != 64)
        r += String.fromCharCode(buf[2]);
      l += i;
    } while (d != 64);

    var result = r.slice(0, l);

    return result;
}
