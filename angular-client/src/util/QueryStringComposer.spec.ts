import { queryStringComposer } from './QueryStringComposer';

it('should work', () => {
  const queryString = queryStringComposer(
    { artist: 'Janet Joplin', title: 'Mercedes Benz' },
    2020
  );

  //TODO: make assertion
});
