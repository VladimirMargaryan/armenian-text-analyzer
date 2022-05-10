$(document).ready(function(){
  $('form button').prop('disabled', true);
  $('form input').change(function () {
    $('form button').prop('disabled', false);
    $('form p').text("ֆայլը ընտրված է: \n" + this.files.item(0).name);
  });

});


const magicalUnderlines = Array.from(document.querySelectorAll('.underline--magical'));

const gradientAPI = 'https://gist.githubusercontent.com/wking-io/3e116c0e5675c8bcad8b5a6dc6ca5344/raw/4e783ce3ad0bcd98811c6531e40256b8feeb8fc8/gradient.json';

const randNumInRange = max => Math.floor(Math.random() * (max - 1));
const mergeArrays = (arrOne, arrTwo) => arrOne
    .map((item, i) => `${item} ${arrTwo[i]}`)
    .join(', ');

const addBackground = (elms) => (color) => {
  elms.forEach(el => {
    el.style.backgroundImage = color;
  });
}
const getData = async(url) => {
  const response = await fetch(url);
  const data = await response.json();
  return data.data;
}

const addBackgroundToUnderlines = addBackground(magicalUnderlines);
const buildGradient = (obj) => `linear-gradient(${obj.direction}, ${mergeArrays(obj.colors, obj.positions)})`;
const applyGradient = async(url, callback) => {
  const data = await getData(url);
  const gradient = buildGradient(data[randNumInRange(data.length)]);
  callback(gradient);
}

applyGradient(gradientAPI, addBackgroundToUnderlines);