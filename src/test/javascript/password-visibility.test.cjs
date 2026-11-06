const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

const inputs = {
  password: { type: "password", value: "Example demo123!" },
  confirm: { type: "password", value: "Example demo123!" },
};
function makeButton(id, label) {
  return {
    attrs: { "aria-controls": id, "aria-pressed": "false" },
    dataset: { passwordToggle: label },
    getAttribute(name) { return this.attrs[name]; },
    setAttribute(name, value) { this.attrs[name] = value; },
    addEventListener(event, handler) { assert.equal(event, "click"); this.click = handler; },
  };
}
const password = makeButton("password", "password");
const confirm = makeButton("confirm", "confirm password");
const missing = makeButton("missing", "missing field");
vm.runInNewContext(
  fs.readFileSync(path.join(__dirname, "../../main/resources/static/js/password-visibility.js"), "utf8"),
  { document: { querySelectorAll: () => [password, confirm, missing], getElementById: id => inputs[id] } },
);
assert.equal(inputs.password.type, "password");
password.click();
assert.equal(inputs.password.type, "text");
assert.equal(inputs.confirm.type, "password");
assert.equal(password.attrs["aria-pressed"], "true");
assert.equal(password.attrs["aria-label"], "Hide password");
confirm.click();
assert.equal(inputs.confirm.type, "text");
assert.equal(confirm.title, "Hide confirm password");
password.click();
confirm.click();
assert.equal(inputs.password.type, "password");
assert.equal(inputs.confirm.type, "password");
assert.equal(password.attrs["aria-label"], "Show password");
assert.equal(confirm.attrs["aria-pressed"], "false");
assert.equal(inputs.password.value, "Example demo123!");
assert.equal(inputs.confirm.value, "Example demo123!");
assert.equal(missing.click, undefined);
console.log("PASS: independent show/hide controls, accessible labels, unchanged values and missing-field handling.");
