// @flow
import fastdom from 'lib/fastdom-promise';
import fetch from 'lib/fetch';

const isCheckbox = element => element.type === 'checkbox';
const isValid = element => element.name !== '';

const formToObj = elements =>
    [].reduce.call(
        elements,
        (data, element) => {
            if (isValid(element)) {
                if (isCheckbox(element)) {
                    data[element.name] = (data[element.name] || []).concat(
                        element.value
                    );
                } else {
                    data[element.name] = element.value;
                }
            }
            return data;
        },
        {}
    );

const showConfirmation = cForm => {
    fastdom.write(() => {
        cForm.textContent = 'Thank you for your contribution';
    });
};

const showError = cForm => {
    fastdom.write(() => {
        cForm.append('Sorry there was an error submitting your contribution');
    });
};

export const submitForm = (e: any) => {
    e.preventDefault();
    const cForm = e.target;
    const data = formToObj(cForm.elements);

    return fetch('/formstack-campaign/submit', {
        method: 'post',
        body: JSON.stringify(data),
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
        },
    }).then(res => {
        if (res.ok) {
            showConfirmation(cForm);
        } else {
            showError(cForm);
        }
    });
};
